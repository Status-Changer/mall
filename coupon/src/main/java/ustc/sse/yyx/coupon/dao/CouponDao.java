package ustc.sse.yyx.coupon.dao;

import ustc.sse.yyx.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 14:51:46
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
