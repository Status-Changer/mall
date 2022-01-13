package ustc.sse.yyx.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递的检索条件
 * 需要进一步进行以下操作：
 * 全文检索（模糊匹配）、排序、过滤（属性、分类、品牌、价格区间、库存等）、分页、高亮、聚合分析
 */
@Data
public class SearchParam {
    private String keyword; // 全文匹配关键字
    private Long catalog3Id; // 三级分类的id

    // 排序条件：saleCount_asc/desc, hotScore_asc/desc, skuPrice_asc/desc
    private String sort;

    private Integer hasStock = 1; // 只显示无/有货 0/1 默认显示有货
    private String skuPriceRange; // 价格区间查询 1_400/_400/1_
    private List<Long> brandId; // 品牌id 可以多选
    private List<String> attrs; // 属性 1_iOS:Android

    private Integer pageNum = 1; // 页码（如果搜索结果太多需要分页）
}















