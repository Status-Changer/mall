package ustc.sse.yyx.product.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.common.validation.AddGroup;
import ustc.sse.yyx.common.validation.UpdateGroup;
import ustc.sse.yyx.common.validation.UpdateStatusGroup;
import ustc.sse.yyx.product.entity.BrandEntity;
import ustc.sse.yyx.product.service.BrandService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Ʒ��
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 16:12:08
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/info")
    public R info(@RequestParam("brandIds") List<Long> brandIdList) {
        List<BrandEntity> brands = brandService.getBrandsByIdList(brandIdList);
        return R.ok().put("brand", brands);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(value = AddGroup.class) @RequestBody BrandEntity brand) {
		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(value = UpdateGroup.class) @RequestBody BrandEntity brand) {
		brandService.updateDetails(brand);
        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(value = UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
