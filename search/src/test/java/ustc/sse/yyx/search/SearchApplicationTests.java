package ustc.sse.yyx.search;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ustc.sse.yyx.search.config.ElasticSearchConfig;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
class SearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void indexData() throws IOException {
        // Construction Part
        IndexRequest indexRequest = new IndexRequest("test");
        indexRequest.id("1");
        String jsonString = JSON.toJSONString(new User("xuan", "M", 22));
        indexRequest.source(jsonString, XContentType.JSON);

        // Execution Part
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // Response Part
        System.out.println(indexResponse);
    }

    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // Construct Query (Aggregation, ...) Conditions
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"))
                .aggregation(AggregationBuilders.terms("ageAggregation").field("age"))
                .aggregation(AggregationBuilders.avg("averageBalanceAggregation").field("balance"));
        System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(searchResponse.toString());

        // Get all hits
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

        // Analysis data
        Aggregations aggregations = searchResponse.getAggregations();

        Terms ageAggregation = aggregations.get("ageAggregation");
        for (Terms.Bucket bucket : ageAggregation.getBuckets()) {
            System.out.println("AGE: " + bucket.getKeyAsString() + ", ==> " + bucket.getDocCount());
        }

        Avg averageBalanceAggregation = aggregations.get("averageBalanceAggregation");
        System.out.println("Average balance: " + averageBalanceAggregation.getValue());
    }

    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Data
    @AllArgsConstructor
    static class User {
        private String username;
        private String gender;
        private Integer age;
    }


    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }



}
