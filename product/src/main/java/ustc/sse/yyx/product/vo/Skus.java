/**
  * Copyright 2021 json.cn 
  */
package ustc.sse.yyx.product.vo;
import lombok.Data;
import ustc.sse.yyx.common.to.MemberPrice;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2021-11-30 17:16:46
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class Skus {
    private List<Attr> attr;
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}