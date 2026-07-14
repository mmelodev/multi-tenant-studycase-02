package br.com.multi_tenant_studycase.controller;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.request.StockMvtRequest;
import br.com.multi_tenant_studycase.response.StockMvtResponse;
import br.com.multi_tenant_studycase.services.StockMvtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock Mvt", description = "Stock Mvt API")
public class StockMvtController {

    private final StockMvtService service;

    @PostMapping
    public ResponseEntity<Void> createStockMvt(
            @RequestBody
            @Valid
            final StockMvtRequest request
    ) {
        this.service.create(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{stock-mvt-id}")
    public ResponseEntity<Void> updateStockMvt(
            @RequestBody
            @Valid
            final StockMvtRequest request,
            @PathVariable("stock-mvt-id")
            @NotNull(message = "Stock Mvt ID cannot be null")
            final String id
    ) {
        this.service.update(id, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{stock-mvt-id}")
    public ResponseEntity<StockMvtResponse> findStockMvtById(
            @PathVariable("stock-mvt-id")
            @NotNull(message = "Stock Mvt ID cannot be null")
            final String id
    ) {
        return ResponseEntity.ok(this.service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<StockMvtResponse>> findAllStockMvts(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(this.service.findAll(page, size));
    }

    @GetMapping("/product/{product-id}")
    public ResponseEntity<PageResponse<StockMvtResponse>> findAllStockMvtsByProductId(
            @PathVariable("product-id")
            @NotNull(message = "Product ID cannot be null")
            final String productId,
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(this.service.findAllByProductId(productId, page, size));
    }

    @DeleteMapping("/{stock-mvt-id}")
    public ResponseEntity<Void> deleteStockMvt(
            @PathVariable("stock-mvt-id")
            @NotNull(message = "Stock Mvt ID cannot be null")
            final String id
    ) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
