package ustc.sse.yyx.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import ustc.sse.yyx.product.entity.CategoryBrandRelationEntity;

/**
 * Ʒ�Ʒ������
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {
    @Update(value = "UPDATE `pms_category_brand_relation` SET catelog_name=#{name} WHERE catelog_id=#{catId}")
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
