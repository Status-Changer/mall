package ustc.sse.yyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.product.entity.AttrAttrgroupRelationEntity;
import ustc.sse.yyx.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * ����&���Է������
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGroupRelationVo> attrGroupRelationVos);
}

