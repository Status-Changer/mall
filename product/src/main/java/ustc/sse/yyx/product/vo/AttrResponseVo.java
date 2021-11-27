package ustc.sse.yyx.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AttrResponseVo extends AttrVo {
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
