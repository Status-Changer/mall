package ustc.sse.yyx.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;

import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.ware.dao.WareSkuDao;
import ustc.sse.yyx.ware.entity.WareSkuEntity;
import ustc.sse.yyx.ware.feign.ProductFeignService;
import ustc.sse.yyx.ware.service.WareSkuService;
import ustc.sse.yyx.ware.vo.SkuHasStockVo;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities =
                this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            // ????????????sku?????????
            // ???????????????????????????????????????????????????
            // 1. try catch??????????????????
            // 2. TODO ??????????????????????????????
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception ignored) {

            }

            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> skuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            // ????????????sku????????????
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

}