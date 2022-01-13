package ustc.sse.yyx.search.service.impl;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ustc.sse.yyx.search.config.ElasticSearchConfig;
import ustc.sse.yyx.search.constant.EsConstant;
import ustc.sse.yyx.search.service.SearchService;
import ustc.sse.yyx.search.vo.SearchParam;
import ustc.sse.yyx.search.vo.SearchResult;

import java.io.IOException;

@Service
public class SearchServiceImpl implements SearchService {
    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public SearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 执行检索请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            // 分析相应数据，并封装成所需要的格式
            searchResult = buildSearchResult(searchResponse);
        } catch (IOException ignored) {
        }
        return searchResult;
    }

    private SearchResult buildSearchResult(SearchResponse searchResponse) {
        SearchResult searchResult = new SearchResult();

//        // 返回的所有查询到的商品
//        searchResult.setProducts();
//
//        // 返回的涉及到的属性信息
//        searchResult.setAttrs();
//
//        // 品牌信息
//        searchResult.setBrands();
//
//        // 分类信息
//        searchResult.setCatalogs();
//
//        searchResult.setPageNum();
//        searchResult.setTotalPages();
//        searchResult.setTotalItems();
        return searchResult;
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /*
         * 模糊匹配，过滤
         */
        // 1. 构建bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1 must模糊匹配
        if (StringUtils.hasLength(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2 filter构建

        // 1.2.1 三级分类id
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }

        // 1.2.2 品牌id
        if (searchParam.getBrandId() != null && !searchParam.getBrandId().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        // 1.2.3 属性 e.g. ...&attrs=1_5寸:8寸&attrs=4_中国移动:中国联通
        if (searchParam.getAttrs() != null && !searchParam.getAttrs().isEmpty()) {
            for (String attr : searchParam.getAttrs()) {
                // e.g. ...&attrs=1_5寸:8寸
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] attrIdValues = attr.split("_");
                String attrId = attrIdValues[0];
                String[] attrValues = attrIdValues[1].split(":");

                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 要为每一个属性提供一个nested查询
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);

                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        // 1.2.4 库存有无
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));

        // 1.2.5 价格区间 xx_xx
        if (StringUtils.hasLength(searchParam.getSkuPriceRange())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPriceRange");
            String[] range = searchParam.getSkuPriceRange().split("_");
            if (range.length == 2) {
                rangeQueryBuilder.gte(range[0]).lte(range[1]);
            } else if (range.length == 1) {
                if (searchParam.getSkuPriceRange().startsWith("_")) {
                    rangeQueryBuilder.lte(range[0]);
                }
                if (searchParam.getSkuPriceRange().endsWith("_")) {
                    rangeQueryBuilder.gte(range[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        // 将上述所有条件封装
        searchSourceBuilder.query(boolQueryBuilder);

        /*
         * 排序，高亮，分页
         */

        // 2.1 排序 xxx_asc/desc
        if (StringUtils.hasLength(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] entryOrder = sort.split("_");
            SortOrder sortOrder = entryOrder[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(entryOrder[0], sortOrder);
        }

        // 2.2 分页
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 2.3 高亮
        if (StringUtils.hasLength(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(searchParam.getKeyword());
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /*
         * 聚合分析
         */

        // 3.1 品牌聚合及其子聚合
        TermsAggregationBuilder brandAggregation = AggregationBuilders.terms("brandAggregation").field("brandId").size(50);
        brandAggregation.subAggregation(AggregationBuilders.terms("brandNameAggregation").field("brandName").size(1));
        brandAggregation.subAggregation(AggregationBuilders.terms("brandImageAggregation").field("brandImg").size(1));

        searchSourceBuilder.aggregation(brandAggregation);

        // 3.2 分类聚合及其子聚合
        TermsAggregationBuilder catalogAggregationBuilder =
                AggregationBuilders.terms("catalogAggregation").field("catalogId").size(20);
        catalogAggregationBuilder.subAggregation(AggregationBuilders.terms("catalogNameAggregation").field("catalogName").size(1));

        searchSourceBuilder.aggregation(catalogAggregationBuilder);

        // 3.3 属性聚合（nested）
        NestedAggregationBuilder attrAggregationBuilder = AggregationBuilders.nested("attrAggregation", "attrs");

        TermsAggregationBuilder attrIdAggregation = AggregationBuilders.terms("attrIdAggregation").field("attrs.attrId");
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrNameAggregation").field("attrs.attrName").size(1));
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrValueAggregation").field("attrs.attrValue").size(50));
        attrAggregationBuilder.subAggregation(attrIdAggregation);

        searchSourceBuilder.aggregation(attrAggregationBuilder);

        System.out.println(searchSourceBuilder);

        return new SearchRequest(new String[] {EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }
}
