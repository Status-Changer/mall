package ustc.sse.yyx.order.dao;

import ustc.sse.yyx.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:16:56
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
