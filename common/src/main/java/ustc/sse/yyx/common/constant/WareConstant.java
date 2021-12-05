package ustc.sse.yyx.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class WareConstant {
    @Getter
    @AllArgsConstructor
    public enum PurchaseStatusEnum {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVED(2, "已领取"),
        FINISHED(3, "已完成"),
        HAS_ERROR(4, "出现异常");

        private final int code;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum PurchaseDetailStatusEnum {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        PURCHASING(2, "正在采购"),
        FINISHED(3, "已完成"),
        HAS_ERROR(4, "出现异常或者采购失败");

        private final int code;
        private final String message;
    }
}
