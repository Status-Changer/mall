package ustc.sse.yyx.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ustc.sse.yyx.product.entity.SkuSaleAttrValueEntity;
import ustc.sse.yyx.product.vo.SkuItemSaleAttrVo;

import java.util.List;

/**
 * sku��������&ֵ
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {
    @Select("SELECT pssav.attr_id attr_id, pssav.attr_name attr_name, GROUP_CONCAT(DISTINCT pssav.attr_value) attr_value " +
            "FROM pms_sku_info psi " +
            "LEFT JOIN pms_sku_sale_attr_value pssav on psi.sku_id = pssav.sku_id " +
            "WHERE psi.spu_id=#{spuId} " +
            "GROUP BY pssav.attr_id, pssav.attr_name")
    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
