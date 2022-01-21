package ustc.sse.yyx.product.vo;

import lombok.Data;
import ustc.sse.yyx.product.entity.SkuImagesEntity;
import ustc.sse.yyx.product.entity.SkuInfoEntity;
import ustc.sse.yyx.product.entity.SpuInfoDescEntity;

import java.util.List;

@Data
public class SkuItemVo {
    private SkuInfoEntity skuInfoEntity;
    private List<SkuImagesEntity> skuImagesEntityList;
    private List<SkuItemSaleAttrVo> saleAttrVoList;
    private SpuInfoDescEntity description;
    private List<SpuItemAttrGroupVo> spuItemAttrGroupVoList;
}
