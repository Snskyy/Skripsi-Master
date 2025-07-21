package com.inventory.inventory.service;


import com.inventory.inventory.dto.product.ProductVariantRequest;
import com.inventory.inventory.dto.product.ProductVariantResponse;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.ProductNotFoundException;
import com.inventory.inventory.exception.VariantNotFoundException;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.model.ProductVariant;
import com.inventory.inventory.model.Stock;
import com.inventory.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductVariantService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockService stockService;

    public ProductVariantResponse createProductVariant(Long shopId, ProductVariantRequest request) {
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new BadRequestException("Harga tidak boleh kosong atau negatif");
        }
        if (request.getMinimumStock() == null || request.getMinimumStock() < 0) {
            throw new BadRequestException("Minimum stock tidak boleh kosong atau negatif");
        }
        // Validasi atribut jika tidak kosong
        if (request.getAttributes() != null && !request.getAttributes().isBlank()) {
            String[] pairs = request.getAttributes().split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length != 2 || keyValue[0].trim().isEmpty() || keyValue[1].trim().isEmpty()) {
                    throw new BadRequestException("Format atribut tidak valid. Setiap pasangan 'key:value' harus lengkap dan tidak boleh kosong.");
                }
            }
        }

        // Validasi productId wajib ada
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID tidak boleh null");
        }

        Long productId = Long.parseLong(request.getProductId());

        // Validasi product dari shop yang sesuai
        Product product = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan atau tidak sesuai dengan shop"));

        // Cek SKU duplikat untuk product ini, hanya jika SKU tidak kosong
        if (request.getSku() != null && !request.getSku().isBlank()) {
            boolean exists = productVariantRepository.existsActiveByProductIdAndSku(productId, request.getSku());
            if (exists) {
                throw new BadRequestException("SKU sudah digunakan untuk produk ini");
            }
        }

        // Buat entitas ProductVariant
        ProductVariant variant = new ProductVariant();
        variant.setProductId(product.getId());
        variant.setSku(request.getSku()); // Boleh kosong/null
        variant.setAttributes(request.getAttributes());
        variant.setPrice(request.getPrice());
        variant.setCreateDate(LocalDateTime.now());
        variant.setMinimumStock(request.getMinimumStock());

        // Simpan varian
        variant = productVariantRepository.save(variant);

        // Buat response
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(product.getId());
        response.setSku(variant.getSku());
        response.setAttributes(variant.getAttributes());
        response.setPrice(variant.getPrice());
        response.setStock(0L);
        response.setMinimumStock(variant.getMinimumStock());
        return response;
    }

    public ProductVariantResponse  updateProductVariant(Long shopId,Long variantId , ProductVariantRequest request) {

        if (variantId == null) {
            throw new BadRequestException("ID varian tidak boleh kosong");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new BadRequestException("Harga tidak boleh kosong atau negatif");
        }
        if (request.getMinimumStock() == null || request.getMinimumStock() < 0) {
            throw new BadRequestException("Minimum stock tidak boleh kosong atau negatif");
        }

        if (request.getAttributes() != null && !request.getAttributes().isBlank()) {
            String[] pairs = request.getAttributes().split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length != 2 || keyValue[0].trim().isEmpty() || keyValue[1].trim().isEmpty()) {
                    throw new BadRequestException("Format atribut tidak valid. Setiap pasangan 'key:value' harus lengkap dan tidak boleh kosong.");
                }
            }
        }
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));
        Product product = productRepository.findByIdAndShopId(variant.getProductId(), shopId)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan atau tidak milik toko"));

        if (request.getSku() != null) variant.setSku(request.getSku());
        if (request.getAttributes() != null) variant.setAttributes(request.getAttributes());
        if (request.getPrice() != null) variant.setPrice(request.getPrice());
        if (request.getMinimumStock() != null) variant.setMinimumStock(request.getMinimumStock());

        variant = productVariantRepository.save(variant);

        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProductId());
        response.setSku(variant.getSku());
        response.setAttributes(variant.getAttributes());
        response.setPrice(variant.getPrice());
        response.setStock(variant.getStock());
        response.setMinimumStock(variant.getMinimumStock());

        return response;
    }

    public void softDeleteVariant(Long variantId, Long shopId, Long userId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));

        // Validasi produk dan kepemilikan toko
        Product product = productRepository.findByIdAndShopId(variant.getProductId(), shopId)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan atau tidak milik toko"));

        // Tandai variant sebagai dihapus
        variant.setDeleteDate(LocalDateTime.now());
        productVariantRepository.save(variant);

        // Tandai semua stok yang terkait variant ini juga sebagai dihapus
        List<Stock> relatedStocks = stockRepository.findActiveByVariantId(variantId);
        for (Stock stock : relatedStocks) {
            stock.setDeleteDate(LocalDateTime.now());
            stockService.recordStockHistory(
                    stock,
                    "OUT",
                    "VARIANT_DELETED",
                    stock.getQuantity(),
                    userId,
                    null,
                    "Varian dihapus, stok dihapus"
            );
        }
        stockRepository.saveAll(relatedStocks);
    }



}
