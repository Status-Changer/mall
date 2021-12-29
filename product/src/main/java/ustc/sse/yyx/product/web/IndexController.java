package ustc.sse.yyx.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ustc.sse.yyx.product.entity.CategoryEntity;
import ustc.sse.yyx.product.service.CategoryService;
import ustc.sse.yyx.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    private final CategoryService categoryService;

    @Autowired
    public IndexController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(value = {"/", "/index.html"})
    public String indexPage(Model model) {
        // TODO 查出所有的一级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevelOneCategories();
        model.addAttribute("categories", categoryEntityList);
        return "index";
    }

    @ResponseBody
    @GetMapping(value = "/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJSON() {
        return categoryService.getCatalogJSON();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
