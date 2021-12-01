package ustc.sse.yyx.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ustc.sse.yyx.common.to.SkuReductionTo;
import ustc.sse.yyx.common.to.SpuBoundsTo;
import ustc.sse.yyx.common.utils.R;

@FeignClient("mall-coupon")
public interface CouponFeignService {
    // 只要JSON数据模型是兼容的，双方服务无需使用同一个TO
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
