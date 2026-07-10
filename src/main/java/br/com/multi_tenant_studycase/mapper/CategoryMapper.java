package br.com.multi_tenant_studycase.mapper;

import br.com.multi_tenant_studycase.entities.Category;
import br.com.multi_tenant_studycase.request.CategoryRequest;
import br.com.multi_tenant_studycase.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity (final CategoryRequest request){
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deleted(false)
                .build();
    }

    public CategoryResponse toResponse (final Category entity){
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
