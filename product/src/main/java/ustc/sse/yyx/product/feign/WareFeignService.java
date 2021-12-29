package ustc.sse.yyx.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ustc.sse.yyx.common.utils.R;

import java.util.List;

@FeignClient("mall-ware")
public interface WareFeignService {
    /**
     * 要获取返回的data字段值，可以：
     * (1) R设计的时候加上泛型；或者
     * (2) 直接返回想要的结果
     * (3) 自行封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasStock")
    R skuHasStock(@RequestBody List<Long> skuIds);
}
