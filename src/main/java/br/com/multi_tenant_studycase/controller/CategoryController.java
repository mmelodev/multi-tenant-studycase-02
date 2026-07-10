package br.com.multi_tenant_studycase.controller;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.request.CategoryRequest;
import br.com.multi_tenant_studycase.response.CategoryResponse;
import br.com.multi_tenant_studycase.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> createCategory(@Valid @RequestBody final CategoryRequest request){
        this.categoryService.create(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{category-id}")
    public ResponseEntity<Void> updateCategory(@Valid @RequestBody final CategoryRequest request, @PathVariable("category-id") final String id){
        this.categoryService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{category-id}")
    public ResponseEntity<CategoryResponse> getAllCategories (@PathVariable ("category-id") final String id){
        return ResponseEntity.ok(this.categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(this.categoryService.findALl(page, size));
    }

    @DeleteMapping("/{category-id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable ("/{category-id}") final String id){
        this.categoryService.delete(id);
        return ResponseEntity.ok().build();
    }
}
