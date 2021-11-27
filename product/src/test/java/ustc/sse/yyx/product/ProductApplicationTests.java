package ustc.sse.yyx.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ustc.sse.yyx.product.service.CategoryService;

import java.util.Arrays;

@SpringBootTest
@Slf4j
class ProductApplicationTests {
    @Autowired
    private CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("路径：{}", Arrays.asList(catelogPath));
    }
    @Test
    void contextLoads() {

    }

}
