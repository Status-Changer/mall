package ustc.sse.yyx.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class ThirdPartyApplicationTests {
    @Autowired
    private OSSClient ossClient;

    @Test
    public void testUpload() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("C:\\Users\\Xuuuuuan\\Desktop\\新建文本文档.txt");
        ossClient.putObject("xuan-mall", "新建文本文档2.txt", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("Success!");
    }
    @Test
    void contextLoads() {
    }

}
