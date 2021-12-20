package ustc.sse.yyx.search.service;

import ustc.sse.yyx.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean setStatusUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
