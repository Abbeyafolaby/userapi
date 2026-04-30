package com.learn.userapi.service;

import com.learn.userapi.dto.request.ProductCreateRequest;
import com.learn.userapi.dto.response.PagedResponse;
import com.learn.userapi.dto.response.ProductResponse;
import com.learn.userapi.exception.DuplicateResourceException;
import com.learn.userapi.exception.ResourceNotFoundException;
import com.learn.userapi.model.Product;
import com.learn.userapi.repository.ProductRepository;
import com.learn.userapi.util.PageableValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    // only these fields can be sorted on — they have database indexes
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("name", "price", "createdAt", "stockQuantity");

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Pageable validated = PageableValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        Page<ProductResponse> page = productRepository
                .findAll(validated)
                .map(ProductResponse::fromProduct);  // Page.map transforms content

        log.info("Retrieved page {} of {} products ({} total)",
                page.getNumber(), page.getTotalPages(), page.getTotalElements());

        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(String name, Pageable pageable) {
        Pageable validated = PageableValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        Page<ProductResponse> page = productRepository
                .findByNameContainingIgnoreCase(name, validated)
                .map(ProductResponse::fromProduct);

        log.info("Search '{}' returned {} products", name, page.getTotalElements());
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Product already exists with name: " + request.getName());
        }
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity()
        );
        ProductResponse created = ProductResponse.fromProduct(
                productRepository.save(product));
        log.info("Product created with id: {}", created.getId());
        return created;
    }

    public ProductResponse updateProduct(Long id, ProductCreateRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStockQuantity(request.getStockQuantity());
        log.info("Product updated with id: {}", id);
        return ProductResponse.fromProduct(productRepository.save(existing));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted with id: {}", id);
    }
}