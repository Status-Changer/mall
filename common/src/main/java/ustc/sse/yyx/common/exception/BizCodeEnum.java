package ustc.sse.yyx.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BizCodeEnum {
    VALIDATION_EXCEPTION(10001, "参数格式校验失败"),
    UNKNOWN_EXCEPTION(10000, "系统未知异常");

    private final int code;
    private final String message;
}