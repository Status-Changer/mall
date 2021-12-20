package ustc.sse.yyx.product.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import ustc.sse.yyx.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * spu��Ϣ
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {
    @Update("UPDATE `pms_spu_info` SET `publish_status`=#{code}, `update_time`=now() WHERE `id`=#{spuId}")
    void updateSpuStatus(@Param("spuId") long spuId, @Param("code") int code);
}
