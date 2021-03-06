package ustc.sse.yyx.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ustc.sse.yyx.common.constant.ProductConstant;
import ustc.sse.yyx.common.to.SkuReductionTo;
import ustc.sse.yyx.common.to.SpuBoundsTo;
import ustc.sse.yyx.common.to.es.SkuEsModel;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;
import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.product.dao.SpuInfoDao;
import ustc.sse.yyx.product.entity.*;
import ustc.sse.yyx.product.feign.CouponFeignService;
import ustc.sse.yyx.product.feign.SearchFeignService;
import ustc.sse.yyx.product.feign.WareFeignService;
import ustc.sse.yyx.product.service.*;
import ustc.sse.yyx.product.vo.*;
import ustc.sse.yyx.ware.vo.SkuHasStockVo;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    private final SpuInfoDescService spuInfoDescService;
    private final SpuImagesService spuImagesService;
    private final AttrService attrService;
    private final ProductAttrValueService productAttrValueService;
    private final SkuInfoService skuInfoService;
    private final SkuImagesService skuImagesService;
    private final SkuSaleAttrValueService skuSaleAttrValueService;
    private final CouponFeignService couponFeignService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final WareFeignService wareFeignService;
    private final SearchFeignService searchFeignService;

    @Autowired
    public SpuInfoServiceImpl(SpuInfoDescService spuInfoDescService,
                              SpuImagesService spuImagesService,
                              AttrService attrService,
                              ProductAttrValueService productAttrValueService,
                              SkuInfoService skuInfoService,
                              SkuImagesService skuImagesService,
                              SkuSaleAttrValueService skuSaleAttrValueService,
                              CouponFeignService couponFeignService,
                              BrandService brandService,
                              CategoryService categoryService,
                              WareFeignService wareFeignService,
                              SearchFeignService searchFeignService) {
        this.spuInfoDescService = spuInfoDescService;
        this.spuImagesService = spuImagesService;
        this.attrService = attrService;
        this.productAttrValueService = productAttrValueService;
        this.skuInfoService = skuInfoService;
        this.skuImagesService = skuImagesService;
        this.skuSaleAttrValueService = skuSaleAttrValueService;
        this.couponFeignService = couponFeignService;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.wareFeignService = wareFeignService;
        this.searchFeignService = searchFeignService;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    // TODO: ??????????????????????????????????????????
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1. ??????SPU???????????? `pms_spu_info`
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2. ??????SPU??????????????? `pms_spu_info_desc`
        List<String> saveVoDecript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", saveVoDecript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3. ??????SPU?????????  `pms_spu_images`
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4. ??????SPU???????????? `pms_product_attr_value`
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveProductAttr(productAttrValueEntities);

        // 5. ??????SPU??????????????? `mall_sms`->`sms_spu_bounds`
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r.getCode() != 0) {
            log.error("Feign: SPU??????????????????");
        }

        // 6. ????????????SPU???????????????SKU??????
        // 6.1 SKU???????????? `pms_sku_info`
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            skus.forEach(sku -> {
                String defaultImageUrl = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImageUrl = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImageUrl);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                // 6.2 SKU???????????? `pms_sku_images`
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                }).filter(skuImagesEntity -> !StringUtils.isEmpty(skuImagesEntity.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                // 6.3 SKU?????????????????? `pms_sku_sale_attr_value`
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4 SKU???????????????????????? `mall_sms`->`sms_sku_ladder`, `sms_sku_full_reduction`, `sms_member_price`
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("Feign: SKU????????????????????????");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> wrapper.eq("id", key).or().like("spu_name", key));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(long spuId) {
        // ?????????????????????

        // ???????????????spuId?????????sku??????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        // ????????????sku??????????????????????????????????????????
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> productAttrValueEntityIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> searchableAttrIds = attrService.selectSearchableAttrIds(productAttrValueEntityIds);
        Set<Long> searchableAttrIdSet = new HashSet<>(searchableAttrIds);
        List<SkuEsModel.Attrs> skuEsModelAttrList = productAttrValueEntities.stream()
                .filter(productAttrValueEntity -> searchableAttrIdSet.contains(productAttrValueEntity.getAttrId()))
                .map(productAttrValueEntity -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(productAttrValueEntity, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());

        // ??????????????????????????????????????????????????????
        Map<Long, Boolean> skuIdHasStockMap = null;
        try {
            List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.skuHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            skuIdHasStockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("Ware Service ERROR: ????????????????????????", e);
        }

        // ????????????
        Map<Long, Boolean> finalSkuIdHasStockMap = skuIdHasStockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());

            // ??????????????????
            if (finalSkuIdHasStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalSkuIdHasStockMap.get(skuInfoEntity.getSkuId()));
            }
            // TODO ???????????????????????????????????? 0
            skuEsModel.setHotScore(0L);

            // ????????????????????????????????????
            BrandEntity brandEntity = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            skuEsModel.setAttrs(skuEsModelAttrList);
            return skuEsModel;
        }).collect(Collectors.toList());

        // TODO ??????????????????ES???????????????search?????????
        R r = searchFeignService.setProductStatusUp(upProducts);
        if (r.getCode() == 0) {
            // ????????????????????????????????????
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP.getCode());
        } else {
            // ??????????????????
            // TODO ????????????????????????????????????????????????
            // Feign ????????????
            // 1. ???????????????????????????????????????JSON??????
            // 2. ???????????????????????????executeAndDecode
            // 3. ??????????????????????????? Retryer
        }
    }
}