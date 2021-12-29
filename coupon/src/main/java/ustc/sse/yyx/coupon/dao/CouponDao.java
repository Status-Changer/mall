package ustc.sse.yyx.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ustc.sse.yyx.coupon.entity.CouponEntity;

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
