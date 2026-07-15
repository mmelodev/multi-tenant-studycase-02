package br.com.multi_tenant_studycase.services;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.request.StockMvtRequest;
import br.com.multi_tenant_studycase.response.StockMvtResponse;

public interface StockMvtService extends BasicService <StockMvtRequest, StockMvtResponse> {
    PageResponse<StockMvtResponse> findAllByProductId(final String productId, final int page, final int size);
}
