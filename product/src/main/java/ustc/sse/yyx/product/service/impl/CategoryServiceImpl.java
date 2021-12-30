package ustc.sse.yyx.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    private final CategoryBrandRelationService categoryBrandRelationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public CategoryServiceImpl(CategoryBrandRelationService categoryBrandRelationService,
                               StringRedisTemplate stringRedisTemplate) {
        this.categoryBrandRelationService = categoryBrandRelationService;
        this.stringRedisTemplate = stringRedisTemplate;
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
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, path);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[0]);
    }

    // 级联更新所有关联的数据
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
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

        // 缓存缺失
        if (!StringUtils.hasLength(catalogJson)) {
            Map<String, List<Catalog2Vo>> catalogJSONFromDatabase = getCatalogJSONFromDatabase();

            // 需要先将查出的对象转换为JSON字符串 相比较于Java序列化 JSON的处理可以跨语言 跨平台兼容
            String catalogJSONString = JSON.toJSONString(catalogJSONFromDatabase);
            stringRedisTemplate.opsForValue().set("catalogJson", catalogJSONString);
            return catalogJSONFromDatabase;
        }

        // 最终得到的是JSON字符串，需要将其转换为Java对象
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
    }


    private Map<String, List<Catalog2Vo>> getCatalogJSONFromDatabase() {
        /* OPTIMIZATION 1：将数据库的多次查询变为一次 */
        // null 表示查询所有的内容 （select *）
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 查出所有一级分类
        List<CategoryEntity> levelOneCategories = getParentCid(categoryEntityList, 0L);

        return levelOneCategories.stream().collect(Collectors.toMap(
                key -> key.getCatId().toString(),
                value -> {
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

    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        // 收集当前节点 id
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
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