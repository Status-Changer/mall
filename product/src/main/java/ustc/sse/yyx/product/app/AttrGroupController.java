package ustc.sse.yyx.product.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.product.entity.AttrEntity;
import ustc.sse.yyx.product.entity.AttrGroupEntity;
import ustc.sse.yyx.product.service.AttrAttrgroupRelationService;
import ustc.sse.yyx.product.service.AttrGroupService;
import ustc.sse.yyx.product.service.AttrService;
import ustc.sse.yyx.product.service.CategoryService;
import ustc.sse.yyx.product.vo.AttrGroupRelationVo;
import ustc.sse.yyx.product.vo.AttrGroupWithAttrsVo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * ���Է���
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 16:12:08
 */
@RestController
@RequestMapping("product/attrgroup")
@ResponseBody
public class AttrGroupController {
    private final AttrGroupService attrGroupService;
    private final CategoryService categoryService;
    private final AttrService attrService;
    private final AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    public AttrGroupController(AttrGroupService attrGroupService,
                               CategoryService categoryService,
                               AttrService attrService,
                               AttrAttrgroupRelationService attrAttrgroupRelationService) {
        this.attrGroupService = attrGroupService;
        this.categoryService = categoryService;
        this.attrService = attrService;
        this.attrAttrgroupRelationService = attrAttrgroupRelationService;
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable(value = "catelogId") Long catelogId) {
        PageUtils pageUtils = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", pageUtils);
    }

    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId) {
        List<AttrEntity> attrEntityList = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", attrEntityList);
    }

    @GetMapping("{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        // 查出当前分类下的所有属性分组
        // 查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", attrGroupWithAttrsVos);
    }


    @GetMapping("{attrGroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrGroupId") Long attrGroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrGroupId);
        return R.ok().put("page", page);
    }


    @PostMapping(value = "/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> attrGroupRelationVos) {
        attrAttrgroupRelationService.saveBatch(attrGroupRelationVos);
        return R.ok();
    }


    @PostMapping(value = "/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos) {
        attrService.deleteRelation(attrGroupRelationVos);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
