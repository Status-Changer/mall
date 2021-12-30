package ustc.sse.yyx.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ustc.sse.yyx.product.service.CategoryService;

import java.util.Arrays;
import java.util.UUID;

@SpringBootTest
@Slf4j
class ProductApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        // 保存
        stringStringValueOperations.set("hello", "world" + UUID.randomUUID());

        // 查询
        System.out.println(stringStringValueOperations.get("hello"));
    }
    @Test
    void contextLoads() {

    }

}
