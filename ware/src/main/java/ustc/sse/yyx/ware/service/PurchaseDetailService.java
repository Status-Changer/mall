package ustc.sse.yyx.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:29:27
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailsByPurchaseId(Long id);
}

