package ustc.sse.yyx.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ustc.sse.yyx.common.to.es.SkuEsModel;
import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.search.config.ElasticSearchConfig;
import ustc.sse.yyx.search.constant.EsConstant;
import ustc.sse.yyx.search.feign.ProductFeignService;
import ustc.sse.yyx.search.service.SearchService;
import ustc.sse.yyx.search.vo.AttrResponseVo;
import ustc.sse.yyx.search.vo.BrandVo;
import ustc.sse.yyx.search.vo.SearchParam;
import ustc.sse.yyx.search.vo.SearchResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private static final String SEARCH_URL_PREFIX = "http://101.43.83.64/search/list.html?";

    private final RestHighLevelClient restHighLevelClient;
    private final ProductFeignService productFeignService;

    @Autowired
    public SearchServiceImpl(RestHighLevelClient restHighLevelClient,
                             ProductFeignService productFeignService) {
        this.restHighLevelClient = restHighLevelClient;
        this.productFeignService = productFeignService;
    }

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        // ??????????????????
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // ??????????????????
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            // ???????????????????????????????????????????????????
            searchResult = buildSearchResult(searchResponse, searchParam);
        } catch (IOException ignored) {
        }
        return searchResult;
    }

    private SearchResult buildSearchResult(SearchResponse searchResponse, SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = searchResponse.getHits();

        // ?????????????????????????????????
        List<SkuEsModel> skuEsModelList = new LinkedList<>();
        SearchHit[] productHits = hits.getHits();
        if (productHits != null && productHits.length != 0) {
            for (SearchHit productHit : productHits) {
                String productSourceString = productHit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(productSourceString, SkuEsModel.class);
                // ??????????????????
                if (StringUtils.hasLength(searchParam.getKeyword())) {
                    HighlightField highlightField = productHit.getHighlightFields().get("skuTitle");
                    String highlightedSkuTitle = highlightField.getFragments()[0].toString();
                    skuEsModel.setSkuTitle(highlightedSkuTitle);
                }
                skuEsModelList.add(skuEsModel);
            }
        }
        searchResult.setProducts(skuEsModelList);

        /*
            ????????????
         */
        // ?????????????????????????????????
        List<SearchResult.AttrVo> attrVoList = new ArrayList<>();
        ParsedNested attrAggregation = searchResponse.getAggregations().get("attrAggregation");
        ParsedLongTerms attrIdAggregation = attrAggregation.getAggregations().get("attrIdAggregation");
        for (Terms.Bucket attrIdAggregationBucket : attrIdAggregation.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // ??????id
            long attrId = attrIdAggregationBucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            // ????????????
            ParsedStringTerms attrNameAggregation = attrIdAggregationBucket.getAggregations().get("attrNameAggregation");
            String attrName = attrNameAggregation.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            // ?????????
            ParsedStringTerms attrValueAggregation = attrIdAggregationBucket.getAggregations().get("attrValueAggregation");
            List<String> attrValueList = attrValueAggregation.getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValueList);

            attrVoList.add(attrVo);
        }
        searchResult.setAttrs(attrVoList);

        // ????????????
        List<SearchResult.BrandVo> brandVoList = new ArrayList<>();
        ParsedLongTerms brandAggregation = searchResponse.getAggregations().get("brandAggregation");
        for (Terms.Bucket brandAggregationBucket : brandAggregation.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // ??????id
            long brandId = brandAggregationBucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            // ?????????
            String brandName = ((ParsedStringTerms) brandAggregationBucket.getAggregations().get("brandNameAggregation"))
                    .getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            // ????????????
            String brandImage = ((ParsedStringTerms) brandAggregationBucket.getAggregations().get("brandImageAggregation"))
                    .getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImage);

            brandVoList.add(brandVo);
        }
        searchResult.setBrands(brandVoList);

        // ????????????
        List<SearchResult.CatalogVo> catalogVoList = new ArrayList<>();
        ParsedLongTerms catalogAggregation = searchResponse.getAggregations().get("catalogAggregation");
        for (Terms.Bucket catalogAggregationBucket : catalogAggregation.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // ??????id
            String catalogIdString = catalogAggregationBucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catalogIdString));
            // ?????????
            ParsedStringTerms catalogNameAggregation = catalogAggregationBucket.getAggregations().get("catalogNameAggregation");
            String catalogName = catalogNameAggregation.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);

            catalogVoList.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVoList);

        // ????????????
        searchResult.setPageNum(searchParam.getPageNum());
        long totalItems = hits.getTotalHits().value;
        searchResult.setTotalItems(totalItems);
        int totalPages = (int) (totalItems + EsConstant.PRODUCT_PAGE_SIZE - 1) / EsConstant.PRODUCT_PAGE_SIZE;
        searchResult.setTotalPages(totalPages);

        // ????????????
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        // ????????????????????????
        if (searchParam.getAttrs() != null && !searchParam.getAttrs().isEmpty()) {
            List<SearchResult.NavVo> navVoList = searchParam.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] indexValue = attr.split("_");
                navVo.setNavValue(indexValue[1]);
                searchResult.getAttrIds().add(Long.parseLong(indexValue[0]));
                R r = productFeignService.getAttrInfo(Long.parseLong(indexValue[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo attrResponseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attrResponseVo.getAttrName());
                } else {
                    navVo.setNavName(indexValue[0]);
                }

                // ??????????????????????????????????????????????????????????????????????????????
                String replacedString = replacedQueryString(searchParam, attr, "attrs");
                navVo.setLink(SEARCH_URL_PREFIX + replacedString);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVoList);
        }

        // ?????????????????????????????????
        if (searchParam.getBrandIdList() != null && !searchParam.getBrandIdList().isEmpty()) {
            List<SearchResult.NavVo> navVoList = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("??????");
            R r = productFeignService.brandInfo(searchParam.getBrandIdList());
            if (r.getCode() == 0) {
                List<BrandVo> brandList = r.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuilder stringBuilder = new StringBuilder();

                String replacedString = "";
                for (BrandVo brandVo : brandList) {
                    stringBuilder.append(brandVo.getBrandName()).append(";");
                    replacedString = replacedQueryString(searchParam, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(stringBuilder.toString());
                navVo.setLink(SEARCH_URL_PREFIX + replacedString);
            }
            navVoList.add(navVo);
        }

        // TODO ?????????????????????????????????

        return searchResult;
    }

    private String replacedQueryString(SearchParam searchParam, String attr, String key) {
        String queryString = searchParam.getQueryString();
        String encodedString = null;
        try {
            // ?????????replace???????????????????????????????????????????????????
            encodedString = URLEncoder.encode(attr, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ignored) {}
        return queryString.replace("&" + key + "=" + encodedString, "");
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /*
         * ?????????????????????
         */
        // 1. ??????bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1 must????????????
        if (StringUtils.hasLength(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2 filter??????

        // 1.2.1 ????????????id
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }

        // 1.2.2 ??????id
        if (searchParam.getBrandIdList() != null && !searchParam.getBrandIdList().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandIdList()));
        }

        // 1.2.3 ?????? e.g. ...&attrs=1_5???:8???&attrs=4_????????????:????????????
        if (searchParam.getAttrs() != null && !searchParam.getAttrs().isEmpty()) {
            for (String attr : searchParam.getAttrs()) {
                // e.g. ...&attrs=1_5???:8???
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] attrIdValues = attr.split("_");
                String attrId = attrIdValues[0];
                String[] attrValues = attrIdValues[1].split(":");

                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // ?????????????????????????????????nested??????
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);

                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        // 1.2.4 ????????????
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));

        // 1.2.5 ???????????? xx_xx
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

        // ???????????????????????????
        searchSourceBuilder.query(boolQueryBuilder);

        /*
         * ????????????????????????
         */

        // 2.1 ?????? xxx_asc/desc
        if (StringUtils.hasLength(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] entryOrder = sort.split("_");
            SortOrder sortOrder = entryOrder[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(entryOrder[0], sortOrder);
        }

        // 2.2 ??????
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 2.3 ??????
        if (StringUtils.hasLength(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(searchParam.getKeyword());
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /*
         * ????????????
         */

        // 3.1 ???????????????????????????
        TermsAggregationBuilder brandAggregation = AggregationBuilders.terms("brandAggregation").field("brandId").size(50);
        brandAggregation.subAggregation(AggregationBuilders.terms("brandNameAggregation").field("brandName").size(1));
        brandAggregation.subAggregation(AggregationBuilders.terms("brandImageAggregation").field("brandImg").size(1));

        searchSourceBuilder.aggregation(brandAggregation);

        // 3.2 ???????????????????????????
        TermsAggregationBuilder catalogAggregationBuilder =
                AggregationBuilders.terms("catalogAggregation").field("catalogId").size(20);
        catalogAggregationBuilder.subAggregation(AggregationBuilders.terms("catalogNameAggregation").field("catalogName").size(1));

        searchSourceBuilder.aggregation(catalogAggregationBuilder);

        // 3.3 ???????????????nested???
        NestedAggregationBuilder attrAggregationBuilder = AggregationBuilders.nested("attrAggregation", "attrs");

        TermsAggregationBuilder attrIdAggregation = AggregationBuilders.terms("attrIdAggregation").field("attrs.attrId");
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrNameAggregation").field("attrs.attrName").size(1));
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrValueAggregation").field("attrs.attrValue").size(50));
        attrAggregationBuilder.subAggregation(attrIdAggregation);

        searchSourceBuilder.aggregation(attrAggregationBuilder);
        return new SearchRequest(new String[] {EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }
}
