package ustc.sse.yyx.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ustc.sse.yyx.common.utils.R;

import java.util.List;

@FeignClient("mall-product")
public interface ProductFeignService {
    @GetMapping("/product/attr/info/{attrId}")
    R getAttrInfo(@PathVariable("attrId") long attrId);

    @GetMapping("/product/brand/info")
    R brandInfo(@RequestParam("brandIds") List<Long> brandIdList);
}
