package ustc.sse.yyx.search.vo;

import lombok.Data;
import ustc.sse.yyx.common.to.es.SkuEsModel;

import java.util.List;

@Data
public class SearchResult {
    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 分页及统计信息
     */
    private Integer pageNum; // 当前页面
    private Integer totalPages; // 总页数
    private Long totalItems; // 总记录数

    /**
     * 当前查询到的结果涉及到的各个内容
     */
    private List<BrandVo> brands; // 品牌
    private List<CatalogVo> catalogs; // 分类
    private List<AttrVo> attrs; // 属性

    @Data
    public static class BrandVo {
        private Integer brandId;
        private String brandImg;
        private String brandName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}
