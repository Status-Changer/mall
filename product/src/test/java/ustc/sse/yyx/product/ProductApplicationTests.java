package ustc.sse.yyx.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ustc.sse.yyx.product.dao.AttrGroupDao;
import ustc.sse.yyx.product.service.SkuSaleAttrValueService;
import ustc.sse.yyx.product.vo.SkuItemSaleAttrVo;
import ustc.sse.yyx.product.vo.SkuItemVo;
import ustc.sse.yyx.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@Slf4j
class ProductApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    public void test() {
//        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(1L, 225L);
//        System.out.println(attrGroupWithAttrsBySpuId);
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(13L);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        // 保存
        stringStringValueOperations.set("hello", "world" + UUID.randomUUID());

        // 查询
        System.out.println(stringStringValueOperations.get("hello"));
    }

    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }

    @Test
    void contextLoads() {

    }

}
