package br.com.multi_tenant_studycase.mapper;

import br.com.multi_tenant_studycase.entities.Category;
import br.com.multi_tenant_studycase.entities.Product;
import br.com.multi_tenant_studycase.request.ProductRequest;
import br.com.multi_tenant_studycase.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity (final ProductRequest request){
        return Product.builder()
                .name(request.getName())
                .reference(request.getReference())
                .description(request.getDescription())
                .price(request.getPrice())
                .alertThreshold(request.getAlertThreshold())
                .category(Category.builder()
                        .id(request.getCategoryId())
                        .build())
                .build();
    }

    public ProductResponse toResponse (final Product product){
        return ProductResponse.builder()
                .name(product.getName())
                .reference(product.getReference())
                .description(product.getDescription())
                .price(product.getPrice())
                .alertThreshold(product.getAlertThreshold())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
