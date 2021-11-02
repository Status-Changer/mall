package ustc.sse.yyx.ware.dao;

import ustc.sse.yyx.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:29:27
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
