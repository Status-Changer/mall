package ustc.sse.yyx.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import ustc.sse.yyx.product.entity.AttrGroupEntity;
import ustc.sse.yyx.product.vo.SkuItemVo;
import ustc.sse.yyx.product.vo.SpuBaseAttrVo;
import ustc.sse.yyx.product.vo.SpuItemAttrGroupVo;

import java.util.List;

/**
 * ���Է���
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {
    @Select("SELECT ppav.spu_id, pag.attr_group_name group_name, pag.attr_group_id, paar.attr_id, pa.attr_name attr_name, ppav.attr_value attr_value " +
            "FROM `pms_attr_group` pag " +
            "LEFT JOIN pms_attr_attrgroup_relation paar on pag.attr_group_id = paar.attr_group_id " +
            "LEFT JOIN pms_attr pa on paar.attr_id = pa.attr_id " +
            "LEFT JOIN pms_product_attr_value ppav on pa.attr_id = ppav.attr_id " +
            "WHERE pag.catelog_id=#{catalogId} AND ppav.spu_id=#{spuId}")
    @Results(id = "spuItemAttrGroupVo", value = {
            @Result(property = "attrs", column = "attrs", many = @Many(select = "ustc.sse.yyx.product.dao.AttrGroupDao.getSpuBaseAttrVoList"))
    })
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);

    @Select("SELECT pa.attr_name, ppav.attr_value " +
            "FROM pms_attr pa " +
            "LEFT JOIN pms_product_attr_value ppav on pa.attr_id = ppav.attr_id " +
            "WHERE ppav.spu_id=#{spuId}")
    @ResultMap(value = "spuItemAttrGroupVo")
    List<SpuBaseAttrVo> getSpuBaseAttrVoList(@Param("spuId") Long spuId);
}
