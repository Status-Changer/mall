package ustc.sse.yyx.common.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpuBoundsTo {
    private Long spuId;
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
}
