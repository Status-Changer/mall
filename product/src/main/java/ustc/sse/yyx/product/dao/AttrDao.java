package ustc.sse.yyx.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ustc.sse.yyx.product.entity.AttrEntity;

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
    List<Long> selectSearchableAttrIds(@Param("productAttrValueEntityIds") List<Long> productAttrValueEntityIds);
}
