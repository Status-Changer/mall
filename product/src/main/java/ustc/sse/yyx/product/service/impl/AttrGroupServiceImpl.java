package ustc.sse.yyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;
import ustc.sse.yyx.product.dao.AttrGroupDao;
import ustc.sse.yyx.product.entity.AttrEntity;
import ustc.sse.yyx.product.entity.AttrGroupEntity;
import ustc.sse.yyx.product.service.AttrGroupService;
import ustc.sse.yyx.product.service.AttrService;
import ustc.sse.yyx.product.vo.AttrGroupWithAttrsVo;
import ustc.sse.yyx.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    private final AttrService attrService;

    @Autowired
    public AttrGroupServiceImpl(AttrService attrService) {
        this.attrService = attrService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        if (catelogId == 0) {
            return queryPage(params);
        }
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> attrGroupEntityQueryWrapper =
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
        if (!StringUtils.isEmpty(key)) {
            attrGroupEntityQueryWrapper.and((obj) ->
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key));
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                attrGroupEntityQueryWrapper);
        return new PageUtils(page);
    }

    // 根据分类ID查出所有分组，以及分组中的所有属性
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 查询所有属性
        return attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrGroupWithAttrsVo);
            List<AttrEntity> attrEntities = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrEntities);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 查出当前spu对应的所有属性的分组信息及当前分组下所有属性对应的值
        return this.getBaseMapper().getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }
}