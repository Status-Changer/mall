package ustc.sse.yyx.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:16:56
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

