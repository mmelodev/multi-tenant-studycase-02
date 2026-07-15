package br.com.multi_tenant_studycase.services.impl;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.entities.Category;
import br.com.multi_tenant_studycase.mapper.CategoryMapper;
import br.com.multi_tenant_studycase.repositories.CategoryRepository;
import br.com.multi_tenant_studycase.request.CategoryRequest;
import br.com.multi_tenant_studycase.response.CategoryResponse;
import br.com.multi_tenant_studycase.exceptions.BusinessException;
import br.com.multi_tenant_studycase.services.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional //é importante para que o tenant correto seja identificado no TenantHibernateFilter
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void create(CategoryRequest request) {
        //verificar se a categoria existe
        checkIfCategoryExistsByName(request.getName());

        final Category category = categoryMapper.toEntity(request);
        this.categoryRepository.save(category);
    }

    @Override
    public void update(String id, CategoryRequest request) {
        final Optional<Category> existingCategory = this.categoryRepository.findById(id);
        if(existingCategory.isEmpty()){
            log.debug("Category Not Found");
            throw new EntityNotFoundException("Category Not Found");
        }

        final Category category = existingCategory.get();

        if(!category.getName().equalsIgnoreCase(request.getName())){
            checkIfCategoryExistsByName(request.getName());
        }

        // Muta a entidade gerenciada para preservar id, campos de auditoria e deleted.
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        this.categoryRepository.save(category);
    }

    @Override
    public PageResponse<CategoryResponse> findALl(final int page, final int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Category> categories = this.categoryRepository.findAll(pageRequest);
        final Page<CategoryResponse> categoryResponses = categories.map(this.categoryMapper::toResponse);
        return PageResponse.of(categoryResponses);
    }

    @Override
    public CategoryResponse findById(String id) {
        return this.categoryRepository.findById(id)
                .map(this.categoryMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));
    }

    @Override
    public void delete(String id) {
        final Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));
        this.categoryRepository.delete(category);
    }

    private void checkIfCategoryExistsByName(String name) {
        final Optional<Category> category = this.categoryRepository.findByNameIgnoreCase(name);
        if(category.isPresent()){
            log.debug("Category already exists");
            throw new BusinessException("Category already exists");
        }
    }
}
