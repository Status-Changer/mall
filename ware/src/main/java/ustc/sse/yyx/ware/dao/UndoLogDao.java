package ustc.sse.yyx.ware.dao;

import ustc.sse.yyx.ware.entity.UndoLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:29:27
 */
@Mapper
public interface UndoLogDao extends BaseMapper<UndoLogEntity> {
	
}
