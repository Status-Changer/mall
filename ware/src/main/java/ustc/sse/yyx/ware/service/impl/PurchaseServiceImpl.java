package ustc.sse.yyx.ware.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import ustc.sse.yyx.common.constant.WareConstant;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;

import ustc.sse.yyx.ware.dao.PurchaseDao;
import ustc.sse.yyx.ware.entity.PurchaseDetailEntity;
import ustc.sse.yyx.ware.entity.PurchaseEntity;
import ustc.sse.yyx.ware.service.PurchaseDetailService;
import ustc.sse.yyx.ware.service.PurchaseService;
import ustc.sse.yyx.ware.service.WareSkuService;
import ustc.sse.yyx.ware.vo.MergeVo;
import ustc.sse.yyx.ware.vo.PurchaseDoneVo;
import ustc.sse.yyx.ware.vo.PurchaseItemDoneVo;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    private final PurchaseDetailService purchaseDetailService;
    private final WareSkuService wareSkuService;

    @Autowired
    public PurchaseServiceImpl(PurchaseDetailService purchaseDetailService,
                               WareSkuService wareSkuService) {
        this.purchaseDetailService = purchaseDetailService;
        this.wareSkuService = wareSkuService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) { // 新建的采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        // TODO 确认采购单状态是0或1
        List<Long> items = mergeVo.getItems();
        final Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().filter(item -> {
            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item);
            return purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode()
                    || purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void receive(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream()
                .map(this::getById)
                .filter(item -> item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                        || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                    item.setUpdateTime(new Date());
                })
                .collect(Collectors.toList());

        // 2. 改变采购单的状态
        this.updateBatchById(purchaseEntities);

        // 3. 改变该采购单采购项的状态
        purchaseEntities.forEach((item) -> {
            List<PurchaseDetailEntity> purchaseDetailEntities =
                    purchaseDetailService.listDetailsByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailEntities.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(purchaseDetailEntity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.PURCHASING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntityList);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        // 1. 改变该采购单中采购项的状态
        boolean flag = true;
        List<PurchaseDetailEntity> updatePurchaseDetailEntities = new ArrayList<>();

        for (PurchaseItemDoneVo purchaseDoneVoItem : purchaseDoneVo.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (purchaseDoneVoItem.getStatus() == WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(purchaseDoneVoItem.getStatus());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());

                // 2. 将成功采购的项进行入库
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(purchaseDoneVoItem.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
            }

            purchaseDetailEntity.setId(purchaseDoneVoItem.getItemId());
            updatePurchaseDetailEntities.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updatePurchaseDetailEntities);

        // 3. 改变采购单状态
        Long id = purchaseDoneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}