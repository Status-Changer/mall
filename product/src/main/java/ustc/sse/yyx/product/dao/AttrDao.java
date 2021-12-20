package ustc.sse.yyx.product.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ustc.sse.yyx.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ��Ʒ����
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
    @Select("<script>SELECT `attr_id` FROM `pms_attr` WHERE `attr_id` IN " +
            "<foreach collection='productAttrValueEntityIds' item='id' seperator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>AND search_type=1</script>")
    List<Long> selectSearchableAttrIds(@Param("productAttrValueEntityIds") List<Long> productAttrValueEntityIds);
}
