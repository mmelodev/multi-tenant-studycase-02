package br.com.multi_tenant_studycase.services.impl;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.entities.Product;
import br.com.multi_tenant_studycase.entities.StockMvt;
import br.com.multi_tenant_studycase.mapper.StockMvtMapper;
import br.com.multi_tenant_studycase.repositories.ProductRepository;
import br.com.multi_tenant_studycase.repositories.StockMvtRepository;
import br.com.multi_tenant_studycase.request.StockMvtRequest;
import br.com.multi_tenant_studycase.response.ProductResponse;
import br.com.multi_tenant_studycase.response.StockMvtResponse;
import br.com.multi_tenant_studycase.services.StockMvtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockMvtServiceImpl implements StockMvtService {

    private final StockMvtRepository stockMvtRepository;
    private final ProductRepository productRepository;
    private final StockMvtMapper stockMvtMapper;

    @Override
    public void create(StockMvtRequest request) {
        checkIfProductExistsByReference(request.getProductId());
        final StockMvt stock = this.stockMvtMapper.toEntity(request);
        this.stockMvtRepository.save(stock);
    }

    @Override
    public void update(final String id, final StockMvtRequest request) {
        final Optional<StockMvt> stockMvt = this.stockMvtRepository.findById(id);
        if(stockMvt.isEmpty()){
            log.debug("Stock Movement not found");
            throw new EntityNotFoundException("Stock Movement not found");
        }

        checkIfProductExistsByReference(request.getProductId());

        final StockMvt stockMvtUpdate = this.stockMvtMapper.toEntity(request);
        stockMvtUpdate.setId(id);
        this.stockMvtRepository.save(stockMvtUpdate);
    }

    @Override
    public PageResponse<StockMvtResponse> findALl(final int page, final int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<StockMvt> stockMvts = this.stockMvtRepository.findAll(pageRequest);
        final Page<StockMvtResponse> stockMvtResponses = stockMvts.map(stockMvtMapper::toResponse);
        return PageResponse.of(stockMvtResponses);
    }

    @Override
    public StockMvtResponse findById(String id) {
        return this.stockMvtRepository.findById(id)
                .map(this.stockMvtMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Stock Movement Not Found"));
    }

    @Override
    public void delete(String id) {
        final StockMvt stockMvt = this.stockMvtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock Movement Not Found"));
        this.stockMvtRepository.delete(stockMvt);
    }

    private void checkIfProductExistsByReference(String name) {
        final Optional<Product> product = this.productRepository.findByReferenceIgnoreCase(name);
        if(product.isPresent()){
            log.debug("Category already exists");
            throw new RuntimeException("Category already exists"); //adicionar exception personalizada
        }
    }
}
