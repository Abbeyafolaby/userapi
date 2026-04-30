package com.learn.userapi.controller;

import com.learn.userapi.dto.request.ProductCreateRequest;
import com.learn.userapi.dto.response.ApiResponse;
import com.learn.userapi.dto.response.PagedResponse;
import com.learn.userapi.dto.response.ProductResponse;
import com.learn.userapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products?page=0&size=10&sort=price,asc
    // GET /api/products?name=laptop&page=0&size=5
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String name) {

        PagedResponse<ProductResponse> response = (name != null && !name.isBlank())
                ? productService.searchProducts(name, pageable)
                : productService.getAllProducts(pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Products retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product retrieved successfully",
                productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully",
                        productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product updated successfully",
                productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Product deleted successfully", null));
    }
}