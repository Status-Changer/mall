package ustc.sse.yyx.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * sku��Ϣ
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Data
@TableName("pms_sku_info")
public class SkuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * skuId
	 */
	@TableId
	private Long skuId;
	/**
	 * spuId
	 */
	private Long spuId;
	/**
	 * sku����
	 */
	private String skuName;
	/**
	 * sku��������
	 */
	private String skuDesc;
	/**
	 * ��������id
	 */
	private Long catalogId;
	/**
	 * Ʒ��id
	 */
	private Long brandId;
	/**
	 * Ĭ��ͼƬ
	 */
	private String skuDefaultImg;
	/**
	 * ����
	 */
	private String skuTitle;
	/**
	 * ������
	 */
	private String skuSubtitle;
	/**
	 * �۸�
	 */
	private BigDecimal price;
	/**
	 * ����
	 */
	private Long saleCount;

}
