package ustc.sse.yyx.product.vo;

import lombok.Data;
import ustc.sse.yyx.product.entity.AttrEntity;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo {
    private Long attrGroupId;
    private String attrGroupName;
    private Integer sort;
    private String descript;
    private String icon;
    private Long catelogId;
    private List<AttrEntity> attrs;
}
