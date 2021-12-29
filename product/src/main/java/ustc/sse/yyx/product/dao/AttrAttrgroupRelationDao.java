package ustc.sse.yyx.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ustc.sse.yyx.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;

/**
 * ����&���Է������
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {
    // 没有<script>标签对就无法运行
    @Delete(value = "<script>DELETE FROM `pms_attr_attrgroup_relation` WHERE " +
            "<foreach collection='attrAttrgroupRelationEntities' item='item' separator=' OR '>" +
            "(`attr_id`=#{item.attrId} AND `attr_group_id`=#{item.attrGroupId})" + "</foreach></script>")
    void deleteBatchRelation(@Param("attrAttrgroupRelationEntities") List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities);
}
