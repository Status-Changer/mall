package ustc.sse.yyx.product.web;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ustc.sse.yyx.product.entity.CategoryEntity;
import ustc.sse.yyx.product.service.CategoryService;
import ustc.sse.yyx.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {
    private final CategoryService categoryService;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public IndexController(CategoryService categoryService,
                           RedissonClient redissonClient,
                           StringRedisTemplate stringRedisTemplate) {
        this.categoryService = categoryService;
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping(value = {"/", "/index.html"})
    public String indexPage(Model model) {
        // TODO 查出所有的一级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevelOneCategories();
        model.addAttribute("categories", categoryEntityList);
        return "index";
    }

    @ResponseBody
    @GetMapping(value = "/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJSON() {
        return categoryService.getCatalogJSON();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // getLock来获得一把锁 通过锁名唯一标识
        RLock helloLock = redissonClient.getLock("helloLock");
        // 阻塞式等待锁
        // 解决了：1) 锁的自动续期，如果业务较长，默认每10s会自动续期至30s，不用担心业务时间长导致锁失效
        // 2) 加锁的业务只要运行完成就不会续期，即使不手动解锁，也不会出现死锁问题
        // 如果显式指定过期时间 比如 helloLock.lock(10, TimeUnit.SECONDS); 就不会自动续期
        helloLock.lock();
        // 而一般实际中更推荐使用显式指定超时时间方法. 给一个较大的过期时间即可
        try {
            System.out.println("加锁成功 执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception ignored) {
        } finally {
            System.out.println("释放锁..." + Thread.currentThread().getId());
            helloLock.unlock();
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        RLock writeLock = readWriteLock.writeLock();
        String uuid = null;
        try {
            writeLock.lock();
            uuid = UUID.randomUUID().toString();
            Thread.sleep(3000);
            stringRedisTemplate.opsForValue().set("writeValue", uuid);
        } catch (Exception ignored) {
        } finally {
            writeLock.unlock();
        }
        return uuid;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        String uuid = null;
        RLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            uuid = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception ignored) {
        } finally {
            readLock.unlock();
        }
        return uuid;
    }

    /**
     * 3个车位，停车前判断是否有位置
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("parking");
        semaphore.acquire();
        return "ok";
    }

    @GetMapping("/leave")
    @ResponseBody
    public String leave() {
        RSemaphore semaphore = redissonClient.getSemaphore("parking");
        semaphore.release();
        return "ok";
    }

    // 等待5个班的所有人离开之后锁门
    @GetMapping("/lock-the-door")
    @ResponseBody
    public String lockTheDoor() throws InterruptedException {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch");
        countDownLatch.trySetCount(5L);
        countDownLatch.await();
        return "放假了";
    }


    @GetMapping("/run/{classId}")
    @ResponseBody
    public String run(@PathVariable("classId") Long classId) {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch");
        countDownLatch.countDown();
        return classId + "已离开";
    }

}
