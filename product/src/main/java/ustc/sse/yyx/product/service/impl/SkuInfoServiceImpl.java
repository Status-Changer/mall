package ustc.sse.yyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;
import ustc.sse.yyx.product.dao.SkuInfoDao;
import ustc.sse.yyx.product.entity.SkuImagesEntity;
import ustc.sse.yyx.product.entity.SkuInfoEntity;
import ustc.sse.yyx.product.entity.SpuInfoDescEntity;
import ustc.sse.yyx.product.service.*;
import ustc.sse.yyx.product.vo.SkuItemSaleAttrVo;
import ustc.sse.yyx.product.vo.SkuItemVo;
import ustc.sse.yyx.product.vo.SpuItemAttrGroupVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("sku_id", key).or().like("sku_name", key));
        }

        String catalogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception ignored) {
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // sku基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setSkuInfoEntity(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // spu销售属性组合
            List<SkuItemSaleAttrVo> skuItemSaleAttrVoList = skuSaleAttrValueService.getSaleAttrsBySpuId(skuInfoEntity.getSpuId());
            skuItemVo.setSaleAttrVoList(skuItemSaleAttrVoList);
        }, threadPoolExecutor);

        CompletableFuture<Void> descriptionCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 获取spu介绍 pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuInfoEntity.getSpuId());
            skuItemVo.setDescription(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> attrCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 获取spu规格参数
            List<SpuItemAttrGroupVo> spuItemAttrGroupVoList =
                    attrGroupService.getAttrGroupWithAttrsBySpuId(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId());
            skuItemVo.setSpuItemAttrGroupVoList(spuItemAttrGroupVoList);
        }, threadPoolExecutor);


        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            // sku图片信息 pms_sku_images
            List<SkuImagesEntity> skuImagesEntityList = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setSkuImagesEntityList(skuImagesEntityList);
        }, threadPoolExecutor);

        // 等待所有任务完成
        CompletableFuture.allOf(
                saleCompletableFuture,
                descriptionCompletableFuture,
                attrCompletableFuture,
                imageCompletableFuture
        ).get();
        return skuItemVo;
    }
}