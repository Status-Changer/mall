package ustc.sse.yyx.coupon.dao;

import ustc.sse.yyx.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 14:51:47
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
