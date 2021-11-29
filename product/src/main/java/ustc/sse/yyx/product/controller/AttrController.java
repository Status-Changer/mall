package ustc.sse.yyx.product.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ustc.sse.yyx.product.entity.AttrEntity;
import ustc.sse.yyx.product.service.AttrService;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.R;
import ustc.sse.yyx.product.vo.AttrGroupRelationVo;
import ustc.sse.yyx.product.vo.AttrResponseVo;
import ustc.sse.yyx.product.vo.AttrVo;


/**
 * ��Ʒ����
 *
 * @author Yuxuan Yang
 * @email yxyang21@mail.ustc.edu.cn
 * @date 2021-11-01 16:12:08
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    private final AttrService attrService;

    @Autowired
    public AttrController(AttrService attrService) {
        this.attrService = attrService;
    }

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@PathVariable("attrType") String attrType,
                          @RequestParam Map<String, Object> params,
                          @PathVariable(value = "catelogId") Long catelogId) {
        PageUtils pageUtils = attrService.queryBaseAttrPage(params, catelogId, attrType);
        return R.ok().put("page", pageUtils);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }




    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
