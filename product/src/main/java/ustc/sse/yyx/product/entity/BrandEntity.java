package ustc.sse.yyx.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import ustc.sse.yyx.common.validation.AddGroup;
import ustc.sse.yyx.common.validation.ListValue;
import ustc.sse.yyx.common.validation.UpdateGroup;
import ustc.sse.yyx.common.validation.UpdateStatusGroup;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * Ʒ��
 * 
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 15:05:40
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Ʒ��id
	 */
	@TableId
	@Null(message = "新增时不能指定品牌id", groups = {AddGroup.class})
	@NotNull(message = "修改时必须指定品牌id", groups = {UpdateGroup.class})
	private Long brandId;
	/**
	 * Ʒ����
	 */
	@NotBlank(message = "品牌名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * Ʒ��logo��ַ
	 */
	@URL(message = "logo必须是合法的URL地址", groups = {AddGroup.class, UpdateGroup.class})
	@NotBlank(groups = {AddGroup.class})
	private String logo;
	/**
	 * ����
	 */
	private String descript;
	/**
	 * ��ʾ״̬[0-����ʾ��1-��ʾ]
	 */
	// 自定义的校验注解和校验器，并将两者关联
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(values = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * ��������ĸ
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-z]$", message = "必须以字母开头", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * ����
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序值必须大于或等于0", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
