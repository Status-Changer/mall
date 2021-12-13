package ustc.sse.yyx.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    // 给容器中注入一个 RestHighLevelClient
    @Bean
    public RestHighLevelClient elasticSearchRestClient() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost("101.43.83.64", 9200, "http")));
    }
}
