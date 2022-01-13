package ustc.sse.yyx.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ustc.sse.yyx.search.service.SearchService;
import ustc.sse.yyx.search.vo.SearchParam;
import ustc.sse.yyx.search.vo.SearchResult;

@Controller
public class SearchController {
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 自动将页面提交过来的所有请求参数封装成指定的对象
     * @param searchParam 查询参数
     * @return 检索结果，包含页面所有信息
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) {
        SearchResult searchResult = searchService.search(searchParam);
        model.addAttribute("result", searchResult);
        return "list";
    }
}
