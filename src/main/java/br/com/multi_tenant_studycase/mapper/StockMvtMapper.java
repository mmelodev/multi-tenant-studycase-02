package br.com.multi_tenant_studycase.mapper;

import br.com.multi_tenant_studycase.entities.Product;
import br.com.multi_tenant_studycase.entities.StockMvt;
import br.com.multi_tenant_studycase.request.StockMvtRequest;
import br.com.multi_tenant_studycase.response.StockMvtResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMvtMapper {
    public StockMvt toEntity(final StockMvtRequest request) {
        return StockMvt.builder()
                .typeMvt(request.getTypeMvt())
                .quantity(request.getQuantity())
                .dateMvt(request.getDateMvt())
                .comment(request.getComment())
                .product(Product.builder().id(request.getProductId()).build())
                .build();
    }

    public StockMvtResponse toResponse(final StockMvt stockMvt) {
        return StockMvtResponse.builder()
                .typeMvt(stockMvt.getTypeMvt())
                .quantity(stockMvt.getQuantity())
                .dateMvt(stockMvt.getDateMvt())
                .comment(stockMvt.getComment())
                .build();
    }
}