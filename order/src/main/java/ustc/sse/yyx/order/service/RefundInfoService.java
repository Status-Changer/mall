package ustc.sse.yyx.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:16:56
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

