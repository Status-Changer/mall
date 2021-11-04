package ustc.sse.yyx.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import ustc.sse.yyx.common.utils.R;

@FeignClient(value = "mall-coupon")
public interface CouponFeignService {
    @GetMapping(value = "/coupon/coupon/member/list")
    R memberCoupons();
}
