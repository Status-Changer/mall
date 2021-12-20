package ustc.sse.yyx.search.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ustc.sse.yyx.common.to.es.SkuEsModel;
import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.search.service.ProductSaveService;
import ustc.sse.yyx.common.exception.BizCodeEnum;

import java.util.List;

@RequestMapping("/search/save")
@RestController
@Slf4j
public class ElasticSearchSaveController {
    private final ProductSaveService productSaveService;

    @Autowired
    public ElasticSearchSaveController(ProductSaveService productSaveService) {
        this.productSaveService = productSaveService;
    }

    @PostMapping("/product")
    public R setProductStatusUp(List<SkuEsModel> skuEsModelList) {
        boolean b;
        try {
            b = productSaveService.setStatusUp(skuEsModelList);
        } catch (Exception e) {
            log.error("ElasticSearchSaveController: 商品上架错误" + e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
    }
}
