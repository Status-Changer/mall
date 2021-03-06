package ustc.sse.yyx.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ProductConstant {
    @Getter
    @AllArgsConstructor
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "商品属性");

        private final int code;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum StatusEnum {
        NEW(0, "新建"),
        UP(1, "上架"),
        DOWN(2, "下架");

        private final int code;
        private final String message;
    }
}
