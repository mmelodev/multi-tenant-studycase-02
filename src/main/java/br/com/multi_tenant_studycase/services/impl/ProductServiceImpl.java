package br.com.multi_tenant_studycase.services.impl;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.entities.Category;
import br.com.multi_tenant_studycase.entities.Product;
import br.com.multi_tenant_studycase.mapper.ProductMapper;
import br.com.multi_tenant_studycase.repositories.CategoryRepository;
import br.com.multi_tenant_studycase.repositories.ProductRepository;
import br.com.multi_tenant_studycase.request.ProductRequest;
import br.com.multi_tenant_studycase.response.ProductResponse;
import br.com.multi_tenant_studycase.services.ProductService;
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
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public void create(final ProductRequest request){
        checkIfProductExistsByReference(request.getName());

        checkifCategoryExistsById(request.getCategoryId());

        final Product product = productMapper.toEntity(request);
        this.productRepository.save(product);
    }


    @Override
    public void update(final String id, ProductRequest request) {
        final Optional<Product> productExists = this.productRepository.findById(id);
        if(productExists.isEmpty()){
            log.debug("Product not found");
            throw new EntityNotFoundException("Product not found");
        }

        checkIfProductExistsByReference(request.getName());

        checkifCategoryExistsById(request.getCategoryId());

        final Product product = this.productMapper.toEntity(request);
        product.setId(id);
        this.productRepository.save(product);
    }

    @Override
    public PageResponse<ProductResponse> findALl(final int page, final int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Product> products = this.productRepository.findAll(pageRequest);
        final Page<ProductResponse> productResponses = products.map(productMapper::toResponse);
        return PageResponse.of(productResponses);
    }

    @Override
    public ProductResponse findById(String id) {
        return this.productRepository.findById(id)
                .map(this.productMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product Not Found"));
    }

    @Override
    public void delete(String id) {
        final Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product Not Found"));
        this.productRepository.delete(product);
    }

    private void checkIfProductExistsByReference(String name) {
        final Optional<Product> product = this.productRepository.findByReferenceIgnoreCase(name);
        if(product.isPresent()){
            log.debug("Category already exists");
            throw new RuntimeException("Category already exists"); //adicionar exception personalizada
        }
    }

    private void checkifCategoryExistsById(String categoryId) {
        final Optional<Category> category = this.categoryRepository.findById(categoryId);
        if(category.isEmpty()){
            log.debug("Category does not exist");
            throw new RuntimeException("Category does not exist");
        }
    }
}
