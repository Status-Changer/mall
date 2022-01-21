package ustc.sse.yyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.product.entity.SkuInfoEntity;
import ustc.sse.yyx.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku��Ϣ
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

