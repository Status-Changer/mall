package ustc.sse.yyx.search.service;

import ustc.sse.yyx.search.vo.SearchParam;
import ustc.sse.yyx.search.vo.SearchResult;

public interface SearchService {
    /**
     *
     * @param searchParam 检索的所有参数
     * @return 检索的结果
     */
    SearchResult search(SearchParam searchParam);
}
