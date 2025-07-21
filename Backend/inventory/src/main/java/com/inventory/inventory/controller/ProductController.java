package com.inventory.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventory.dto.product.*;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ProductService;
import com.inventory.inventory.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ObjectMapper objectMapper;

    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<Void> softDeleteProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        productService.softDelete(productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}/update")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest request,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        productService.updateProductOnly(productId, request);
        ProductDetailResponse updatedDetail = productService.getProductDetailById(productId);
        return ResponseEntity.ok(updatedDetail);
    }

    @GetMapping("")
    public ResponseEntity<List<ProductListItemResponse>> getProductsByShop(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<ProductListItemResponse> products = productService.getProductsByShopId(shopId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        ProductDetailResponse detail = productService.getProductDetailById(productId);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/uncategorized")
    public ResponseEntity<List<ProductCategoryResponse>> getUncategorizedProducts(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<ProductCategoryResponse> products = productService.getUncategorizedProducts(shopId);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{productId}/category")
    public ResponseEntity<?> updateProductCategory(
            @PathVariable Long productId,
            @RequestBody ProductCategoryRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        ProductCategoryResponse response = productService.updateProductCategory(
                productId, shopId, request.getCategoryId()
        );
        return ResponseEntity.ok(response);
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        try {
            shopService.shopValidationWithUserId(user.getId(), shopId);
            if (images != null && images.length > 0) {
                System.out.println("Jumlah gambar: " + images.length);
                for (int i = 0; i < images.length; i++) {
                    System.out.println("Gambar[" + i + "]: " + images[i].getOriginalFilename());
                }
            } else {
                System.out.println("Tidak ada gambar dikirim.");
            }
            ProductRequest productRequest = objectMapper.readValue(productJson, ProductRequest.class);
            ProductListItemResponse response = productService.createProduct(user.getId(), productRequest, images);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Gagal membuat produk: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

}
