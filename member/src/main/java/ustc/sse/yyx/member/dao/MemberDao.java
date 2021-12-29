package ustc.sse.yyx.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ustc.sse.yyx.member.entity.MemberEntity;

/**
 * 会员
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-02 15:02:27
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
