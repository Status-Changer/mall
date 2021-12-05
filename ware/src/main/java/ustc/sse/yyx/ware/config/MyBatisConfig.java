package ustc.sse.yyx.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan("ustc.sse.yyx.ware.dao")
@Configuration
public class MyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页的操作。true会返回首页，false继续请求
        paginationInterceptor.setOverflow(true);
        // 设置每页最大条数
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
