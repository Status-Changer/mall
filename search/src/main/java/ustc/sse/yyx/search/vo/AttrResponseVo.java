package ustc.sse.yyx.search.vo;

import lombok.Data;

@Data
public class AttrResponseVo {
    private Long attrId;
    private String attrName;

    private Integer searchType;
    private String icon;

    private String valueSelect;
    private Integer attrType;
    private Long enable;
    private Long catelogId;
    private Integer showDesc;
    private Integer valueType;

    private Long attrGroupId;

    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
