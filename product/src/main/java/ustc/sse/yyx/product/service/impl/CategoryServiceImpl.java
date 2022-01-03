package ustc.sse.yyx.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;
import ustc.sse.yyx.product.dao.CategoryDao;
import ustc.sse.yyx.product.entity.CategoryEntity;
import ustc.sse.yyx.product.service.CategoryBrandRelationService;
import ustc.sse.yyx.product.service.CategoryService;
import ustc.sse.yyx.product.vo.Catalog2Vo;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    private final CategoryBrandRelationService categoryBrandRelationService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Autowired
    public CategoryServiceImpl(CategoryBrandRelationService categoryBrandRelationService,
                               StringRedisTemplate stringRedisTemplate,
                               RedissonClient redissonClient) {
        this.categoryBrandRelationService = categoryBrandRelationService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查找出所有分类
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        // 2. 组装成父子的树形结构
        // 2.1 找到所有的一级分类

        return entityList.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .peek((menu) -> menu.setChildren(getChildren(menu, entityList)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO: 检查当前删除的菜单是否被其他地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catalogId, path);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[0]);
    }

    // 级联更新所有关联的数据
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        // 同时修改（双写）或者删除（失效）缓存中的数据
        // 这里采取的一致性方案：1. 缓存所有数据有过期时间，过期后触发主动更新
        // 2. 读写数据时加上分布式的读写锁（对多读少写的业务性能影响极小）
    }

    @Override
    public List<CategoryEntity> getLevelOneCategories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    // TODO 压力测试时会产生堆外内存溢出：OutOfDirectMemoryError
    // 是由于lettuce客户端的bug导致Netty操作的堆外内存异常
    // 在没有显式指定堆外内存时，默认会使用-Xmx的值 是由于没有及时的内存释放
    // 而可以通过 -Dio.netty.maxDirectMemory 进行设置
    // SOLUTION: 不能只调大堆外内存，而是 1) 升级lettuce客户端 2) 使用jedis客户端
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJSON() {
        /* 加入缓存逻辑 */
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");

        /* 缓存可能导致的问题及解决方案：
            1. 空结果缓存（防止缓存穿透）
            2. 设置加上随机值的过期时间（防止缓存雪崩）
            3. 加锁（解决缓存击穿），得到锁之后再去缓存中确认一次
                3.1 synchronized (this)
                    SpringBoot的所有组件在容器中都是单例，单机环境可行，分布式环境不可行，因为本地锁只能锁住当前进程
                3.2 直接给方法加上synchronized
        */
        // 缓存缺失
        // 一定要将查找数据库和放入缓存整合为一个原子操作 否则有锁的时序不同步问题导致不必要的多次数据库查询
        if (!StringUtils.hasLength(catalogJson)) {
            return getCatalogJSONFromDatabaseWithRedissonLock();
        }

        // 最终得到的是JSON字符串，需要将其转换为Java对象
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
    }

    private Map<String, List<Catalog2Vo>> getCatalogJSONFromDatabaseWithRedissonLock() {
        // 注意锁的名字（粒度） 一般是粒度越细越快
        // 具体缓存的是某个数据要通过某种方式区分（如product-11-lock）
        RLock lock = redissonClient.getLock("catalogJson-lock");
        Map<String, List<Catalog2Vo>> catalogMap;
        lock.lock();
        // 缓存中的数据如何与数据库中保持一致 1.双写模式 2.失效模式
        try {
            catalogMap = getCatalogJSONFromCacheOrDatabase();
        } finally {
            lock.unlock();
        }
        return catalogMap;
    }

    @Deprecated
    private Map<String, List<Catalog2Vo>> getCatalogJSONFromDatabaseWithRedisLock() {
        // 设置锁的过期时间 防止在执行业务逻辑的时候出现异常或者断电导致的死锁
        // 设置锁的过期时间和加锁必须是原子操作 否则在这两个操作之间也有出现死锁的可能性
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, Duration.ofSeconds(300));

        // 获取分布式锁成功
        if (Boolean.TRUE.equals(lock)) {
            Map<String, List<Catalog2Vo>> catalogMap;
            try {
                 catalogMap = getCatalogJSONFromCacheOrDatabase();
            } finally {
                // 删除锁 有可能业务耗时较大或者超时等导致锁已经过期，会将其他线程占用的锁错误的删除
                // 因此先对比值 再删除 注意这也必须是原子操作
                // 使用Lua脚本来解锁
                String deleteScript = "if redis.call(\"get\", KEYS[1]) == ARGV[1] " +
                        "then return redis.call(\"del\", KEYS[1]) else return 0 end";
                // 以下语句 使用上述脚本 将锁原子性删除
                stringRedisTemplate.execute(new DefaultRedisScript<>(deleteScript, Long.class),
                        Collections.singletonList("lock"), uuid);
            }
            return catalogMap;
        } else {
            // 否则要不断重试（自旋），类似synchronized
            // TODO 休眠一段时间后再重试， 如100ms
            try {
                Thread.sleep(200);
            } catch (Exception ignored) {}
            return getCatalogJSONFromDatabaseWithRedisLock();
        }
    }

    @Deprecated
    private Map<String, List<Catalog2Vo>> getCatalogJSONFromDatabaseWithLocalLock() {
        /* OPTIMIZATION 1：将数据库的多次查询变为一次 */
        // null 表示查询所有的内容 （select *）
        // TODO 使用分布式锁来进一步减少数据库查询次数
        synchronized (this) {
            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJson");
            if (StringUtils.hasLength(catalogJSON)) {
                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
            }

            Map<String, List<Catalog2Vo>> catalogJSONFromDatabase = getCatalogJSONFromCacheOrDatabase();

            // 从数据库中查到了数据之后，将数据放入缓存
            String catalogJSONString = JSON.toJSONString(catalogJSONFromDatabase);
            stringRedisTemplate.opsForValue().set("catalogJson", catalogJSONString, 1, TimeUnit.DAYS);
            return catalogJSONFromDatabase;
        }
    }

    private Map<String, List<Catalog2Vo>> getCatalogJSONFromCacheOrDatabase() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.hasLength(catalogJSON)) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
        }
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 查出所有一级分类
        List<CategoryEntity> levelOneCategories = getParentCid(categoryEntityList, 0L);

        return levelOneCategories.stream().collect(
                Collectors.toMap(key -> key.getCatId().toString(), value -> {
                            // 查到这个一级分类的所有二级分类
                            List<CategoryEntity> categoryEntities = getParentCid(categoryEntityList, value.getCatId());
                            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
                            if (categoryEntities != null) {
                                // 给二级分类找到三级分类
                                catalog2Vos = categoryEntities.stream().map(level2Catalog -> {
                                    Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), null,
                                            level2Catalog.getCatId().toString(), level2Catalog.getName());
                                    List<CategoryEntity> level3Catalogs = getParentCid(categoryEntityList, level2Catalog.getCatId());
                                    if (level3Catalogs != null) {
                                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalogs.stream().map(level3Catalog -> {
                                            // 封装成指定格式
                                            return new Catalog2Vo.Catalog3Vo(level2Catalog.getCatId().toString(),
                                                    level3Catalog.getCatId().toString(), level3Catalog.getName());
                                        }).collect(Collectors.toList());
                                        catalog2Vo.setCatalog3List(catalog3Vos);
                                    }
                                    return catalog2Vo;
                                }).collect(Collectors.toList());
                            }
                            return catalog2Vos;
                        }
        ));
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryEntityList, Long parentCid) {
        return categoryEntityList.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid))
                .collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catalogId, List<Long> path) {
        // 收集当前节点 id
        path.add(catalogId);
        CategoryEntity byId = this.getById(catalogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> allEntities) {
        return allEntities.stream().filter(entity -> Objects.equals(entity.getParentCid(), categoryEntity.getCatId()))
                .peek(entity -> entity.setChildren(getChildren(entity, allEntities))) // 递归地找到子菜单
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());

    }

}