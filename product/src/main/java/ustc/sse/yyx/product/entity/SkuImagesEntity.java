package ustc.sse.yyx.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * skuͼƬ
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Data
@TableName("pms_sku_images")
public class SkuImagesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * ͼƬ��ַ
	 */
	private String imgUrl;
	/**
	 * ����
	 */
	private Integer imgSort;
	/**
	 * Ĭ��ͼ[0 - ����Ĭ��ͼ��1 - ��Ĭ��ͼ]
	 */
	private Integer defaultImg;

}
