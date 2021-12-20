package ustc.sse.yyx.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ustc.sse.yyx.common.to.es.SkuEsModel;
import ustc.sse.yyx.common.utils.R;

import java.util.List;

@FeignClient("mall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    public R setProductStatusUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
