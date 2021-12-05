package ustc.sse.yyx.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ustc.sse.yyx.common.utils.R;

@FeignClient("mall-product")
public interface ProductFeignService {
    // 如果加上/api就是给网关发请求
    // 否则就是直接发给对应的机器
    @GetMapping("product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
