package com.inventory.inventory.service;

import com.inventory.inventory.dto.product.*;
import com.inventory.inventory.dto.stock.StockEntryRequest;
import com.inventory.inventory.dto.stock.StockSummaryResponse;
import com.inventory.inventory.exception.*;
import com.inventory.inventory.model.*;
import com.inventory.inventory.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductImageRepository productImageRepository;


    public void softDelete(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));

        List<ProductImage> images = productImageRepository.findByProductId(productId);
        for (ProductImage img : images) {
            String publicId = extractPublicId(img.getImageUrl());
            cloudinaryService.deleteImage(publicId);
        }
        product.setDeleteDate(LocalDateTime.now());
        productRepository.save(product);
        List<Stock> stocks = stockRepository.findActiveByProductId(productId);
        for (Stock stock : stocks) {
            stock.setDeleteDate(LocalDateTime.now());
        }
        stockRepository.saveAll(stocks);
    }

    public String extractPublicId(String imageUrl) {
        URI uri = URI.create(imageUrl);
        String path = uri.getPath();
        String[] parts = path.split("/");
        int index = Arrays.asList(parts).indexOf("upload");
        String publicIdWithExt = String.join("/", Arrays.copyOfRange(parts, index + 2, parts.length));
        return publicIdWithExt.replaceAll("\\.[^.]+$", ""); // hapus ekstensi
    }

    public ProductDetailResponse getProductDetailById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    return new ProductNotFoundException("Product not found");
                });
        ProductDetailResponse dto = new ProductDetailResponse();
        dto.setProduct(toProductResponse(product));


        List<StockSummaryResponse> stockResponses = new ArrayList<>();

        // Ambil thumbnail
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        String thumbnailUrl = images.stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
        dto.setThumbnailUrl(thumbnailUrl);

        if (Boolean.TRUE.equals(product.getHasVariant())) {
            List<ProductVariant> variants = variantRepository.findByProductIdAndDeleteDateIsNull(product.getId());
            List<ProductVariantResponse> variantResponses = new ArrayList<>();

            for (ProductVariant variant : variants) {
                List<Stock> variantStock = stockRepository.findActiveByVariantId(variant.getId());

                // Hitung quantity dan usedQuantity dari stok aktif
                long totalQty = variantStock.stream()
                        .mapToLong(s -> s.getQuantity() != null ? s.getQuantity() : 0)
                        .sum();
                long usedQty = variantStock.stream()
                        .mapToLong(s -> s.getUsedQuantity() != null ? s.getUsedQuantity() : 0)
                        .sum();

                long availableQty = totalQty - usedQty;

                // Build response
                ProductVariantResponse variantResp = toVariantResponse(variant);
                variantResp.setStock(availableQty); // ✅ ini yang penting
                variantResponses.add(variantResp);

                for (Stock stock : variantStock) {
                    stockResponses.add(toStockResponse(stock, variant));
                }
            }

            dto.setVariants(variantResponses);
        } else {
            List<Stock> productStock = stockRepository.findActiveByProductId(productId);
            for (Stock stock : productStock) {
                stockResponses.add(toStockResponse(stock));
            }
        }

        dto.setStockEntries(stockResponses);

        long totalStock = stockResponses.stream()
                .filter(Objects::nonNull)
                .mapToLong(s -> s.getQuantity() != null ? s.getQuantity() : 0)
                .sum();
        dto.setTotalStock(totalStock);
        return dto;
    }

    public List<ProductListItemResponse> getProductsByShopId(Long shopId) {
        List<Product> products = productRepository.findActiveByShopId(shopId);
        List<ProductListItemResponse> responses = new ArrayList<>();

        for (Product product : products) {
            boolean hasVariant = Boolean.TRUE.equals(product.getHasVariant());
            long totalStock = 0L;
            List<ProductVariantSummary> variantSummaries = new ArrayList<>();

            if (hasVariant) {
                List<ProductVariant> variants = variantRepository.findByProductIdAndDeleteDateIsNull(product.getId());

                for (ProductVariant variant : variants) {
                    Long availableStock = stockRepository.sumAvailableStockByVariantId(variant.getId());
                    long stockValue = availableStock != null ? availableStock : 0L;

                    variant.setStock(stockValue); // update cache
                    variantRepository.save(variant);

                    ProductVariantSummary summary = new ProductVariantSummary();
                    summary.setVariantId(variant.getId());
                    summary.setSku(variant.getSku());
                    summary.setMinimumStock(variant.getMinimumStock());
                    summary.setAttributes(parseAttributes(variant.getAttributes()));
                    summary.setStock(stockValue);

                    variantSummaries.add(summary);
                    totalStock += stockValue;
                }
            } else {
                Long availableStock = stockRepository.sumAvailableStockByProductId(product.getId());
                totalStock = availableStock != null ? availableStock : 0L;

                product.setStock(totalStock); // update cache
                productRepository.save(product);
            }


            // Ambil nama kategori (jika ada)
            String categoryName = "-";
            if (product.getCategoryId() != null) {
                Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
                if (category != null) {
                    categoryName = category.getName();
                }
            }

            // Ambil thumbnail (gambar urutan pertama)
            String thumbnailUrl = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(product.getId())
                    .stream()
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElse(null);

            ProductListItemResponse response = new ProductListItemResponse();
            response.setProductId(product.getId());
            response.setName(product.getName());
            response.setMinimumStock(product.getMinimumStock());
            response.setDescription(product.getDescription());
            response.setCategoryName(categoryName);
            response.setHasVariant(hasVariant);
            response.setTotalStock(totalStock);
            response.setVariants(variantSummaries);
            response.setThumbnailUrl(thumbnailUrl);

            responses.add(response);
        }

        return responses;
    }

    public void updateProductOnly(Long productId, ProductUpdateRequest request) {
        if (productId == null) {
            throw new IllegalArgumentException("Produk ID tidak boleh null");
        }
        if(request.getName() == null || request.getName().equalsIgnoreCase("")){
            throw new BadRequestException("Nama produk tidak boleh kosong");
        }
        Long minimumStock = request.getMinimumStock() != null ? request.getMinimumStock() : 0L;
        Long price = request.getPrice() != null ? request.getPrice() : 0L;

        if (minimumStock < 0L) {
            throw new BadRequestException("Minimum stock tidak boleh kurang dari 0");
        }
        if (price < 0L) {
            throw new BadRequestException("Harga tidak boleh kurang dari 0");
        }




        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));

        product.setName(request.getName());
        product.setMinimumStock(request.getMinimumStock());
        product.setDescription(request.getDescription());
        product.setUpdateDate(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Kategori tidak ditemukan"));
            product.setCategoryId(request.getCategoryId());
        } else {
            product.setCategoryId(null);
        }



        productRepository.save(product);
    }

    @Transactional
    public ProductListItemResponse createProduct(Long userId, ProductRequest request, MultipartFile[] images) {
        if(request.getName()==null||request.getName().equalsIgnoreCase("")){
            throw new BadRequestException("Nama product tidak boleh kosong");
        }
        // Validasi minimumStock dan price jika hasVariant = false
        if (Boolean.FALSE.equals(request.getHasVariant())) {
            if (request.getMinimumStock() == null || request.getMinimumStock() < 0) {
                throw new BadRequestException("Minimum stock tidak boleh kosong atau negatif");
            }
            if (request.getPrice() == null || request.getPrice() < 0) {
                throw new BadRequestException("Harga tidak boleh kosong atau negatif");
            }
        }

        // Validasi setiap variant jika hasVariant = true
        if (Boolean.TRUE.equals(request.getHasVariant())) {
            if (request.getVariants() != null) {
                for (int i = 0; i < request.getVariants().size(); i++) {
                    ProductVariantRequest variant = request.getVariants().get(i);

                    // Validasi atribut tidak kosong dan format key:value
                    if (variant.getAttributes() == null || variant.getAttributes().trim().isEmpty()) {
                        throw new BadRequestException("Atribut varian tidak boleh kosong");
                    }

                    String[] pairs = variant.getAttributes().split(",");
                    for (String pair : pairs) {
                        String[] keyVal = pair.split(":", 2);
                        if (keyVal.length != 2 || keyVal[0].trim().isEmpty() || keyVal[1].trim().isEmpty()) {
                            throw new BadRequestException("Format atribut varian tidak valid. Harus key:value dan tidak boleh kosong");
                        }
                    }

                    if (variant.getPrice() == null || variant.getPrice() < 0) {
                        throw new BadRequestException("Harga varian tidak boleh kosong atau negatif");
                    }

                    if (variant.getMinimumStock() == null || variant.getMinimumStock() < 0) {
                        throw new BadRequestException("Minimum stock varian tidak boleh kosong atau negatif");
                    }

                    // Validasi stockEntries dalam variant
                    if (variant.getStockEntries() != null) {
                        for (int j = 0; j < variant.getStockEntries().size(); j++) {
                            StockEntryRequest stock = variant.getStockEntries().get(j);
                            if (stock.getLocation() == null || stock.getLocation().trim().isEmpty()) {
                                throw new BadRequestException("Lokasi stok varian tidak boleh kosong");
                            }
                            if (stock.getQuantity() == null || stock.getQuantity() < 0) {
                                throw new BadRequestException("Jumlah stok varian tidak boleh kosong atau negatif");
                            }
                        }
                    }
                }
            }
        }

        // Validasi stockEntries jika tidak menggunakan varian
        if (Boolean.FALSE.equals(request.getHasVariant())) {
            if (request.getStockEntries() != null) {
                for (StockEntryRequest stock : request.getStockEntries()) {
                    if (stock.getLocation() == null || stock.getLocation().trim().isEmpty()) {
                        throw new BadRequestException("Lokasi stok produk tidak boleh kosong");
                    }
                    if (stock.getQuantity() == null || stock.getQuantity() < 0) {
                        throw new BadRequestException("Jumlah stok produk tidak boleh kosong atau negatif");
                    }
                }
            }
        }


        // 1. Create and save product
        Product product = new Product();
        product.setShopId(request.getShopId());

        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setHasVariant(request.getHasVariant());
        product.setMinimumStock(request.getMinimumStock());
        product.setCreateDate(LocalDateTime.now());
        product.setUpdateDate(LocalDateTime.now());

        if (Boolean.FALSE.equals(request.getHasVariant())) {
            product.setPrice(request.getPrice());
        }

        productRepository.save(product); // ⬅️ SIMPAN DULU untuk mendapatkan ID

        // 2. Upload dan simpan gambar
        int order = 1;
        if (images != null && images.length > 0) {
            for (MultipartFile img : images) {
                try {
                    String url = cloudinaryService.uploadImage(img);

                    ProductImage image = new ProductImage();
                    image.setProductId(product.getId());
                    image.setImageUrl(url);
                    image.setDisplayOrder(order++);
                    image.setCreateDate(LocalDateTime.now());

                    productImageRepository.save(image);
                } catch (IOException e) {
                    throw new RuntimeException("Gagal upload gambar: " + img.getOriginalFilename(), e);
                }
            }
        }

        // 3. Proses varian dan stok seperti biasa (tidak diubah)
        long totalProductStock = 0;

        if (Boolean.TRUE.equals(request.getHasVariant())) {
            if (request.getVariants() != null) {
                for (ProductVariantRequest variantReq : request.getVariants()) {
                    ProductVariant variant = new ProductVariant();
                    variant.setProductId(product.getId());
                    variant.setSku(variantReq.getSku());
                    variant.setAttributes(variantReq.getAttributes());
                    variant.setMinimumStock(variantReq.getMinimumStock());
                    variant.setPrice(variantReq.getPrice());
                    variant.setCreateDate(LocalDateTime.now());
                    variant.setUpdateDate(LocalDateTime.now());

                    variantRepository.save(variant);

                    long totalVariantStock = 0;

                    if (variantReq.getStockEntries() != null) {
                        for (StockEntryRequest stockReq : variantReq.getStockEntries()) {
                            Stock stock = new Stock();
                            stock.setProductId(product.getId());
                            stock.setVariantId(variant.getId());
                            stock.setQuantity(stockReq.getQuantity());
                            stock.setLocation(stockReq.getLocation());
                            stock.setCreateDate(LocalDateTime.now());
                            stock.setUsedQuantity(0L);
                            stock.setDisabled(0L);
                            stock.setShopId(request.getShopId());
                            stock.setUpdateDate(LocalDateTime.now());

                            stockRepository.save(stock);
                            stockService.recordStockHistory(stock, "IN", "PRODUCT_VARIANT_CREATE", stock.getQuantity(), userId, null, null);
                            totalVariantStock += stockReq.getQuantity();
                        }
                    }

                    variant.setStock(totalVariantStock);
                    variantRepository.save(variant);

                    totalProductStock += totalVariantStock;
                }
            }
        } else {
            if (request.getStockEntries() != null) {
                for (StockEntryRequest stockReq : request.getStockEntries()) {
                    Stock stock = new Stock();
                    stock.setId(null); // agar Hibernate tidak pakai nilai id dari request jika ada

                    stock.setProductId(product.getId());
                    stock.setQuantity(stockReq.getQuantity());
                    stock.setLocation(stockReq.getLocation());
                    stock.setCreateDate(LocalDateTime.now());
                    stock.setUsedQuantity(0L);
                    stock.setDisabled(0L);
                    stock.setShopId(request.getShopId());
                    stock.setUpdateDate(LocalDateTime.now());

                    stockRepository.save(stock);
                    stockService.recordStockHistory(stock, "IN", "PRODUCT_CREATE", stock.getQuantity(), userId, null, null);
                    totalProductStock += stockReq.getQuantity();
                }
            }
        }

        // 4. Update total stok
        product.setStock(totalProductStock);
        productRepository.save(product);
// Build response
        ProductListItemResponse response = new ProductListItemResponse();
        response.setProductId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setHasVariant(product.getHasVariant());
        response.setTotalStock(product.getStock());

// Ambil nama kategori
        String categoryName = "-";
        if (product.getCategoryId() != null) {
            categoryName = categoryRepository.findById(product.getCategoryId())
                    .map(Category::getName)
                    .orElse("-");
        }
        response.setCategoryName(categoryName);

// Ambil thumbnail (gambar pertama)
        productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .findFirst()
                .ifPresent(img -> response.setThumbnailUrl(img.getImageUrl()));

// Ringkas varian (jika ada)
        if (Boolean.TRUE.equals(product.getHasVariant())) {
            List<ProductVariant> variantEntities = variantRepository.findByProductIdAndDeleteDateIsNull(product.getId());
            List<ProductVariantSummary> variantSummaries = variantEntities.stream().map(v -> {
                ProductVariantSummary summary = new ProductVariantSummary();
                summary.setVariantId(v.getId());
                summary.setSku(v.getSku());
                summary.setStock(v.getStock());

                // parse string jadi Map<String, String>
                Map<String, String> attributeMap = Arrays.stream(v.getAttributes().split(","))
                        .map(pair -> pair.split(":", 2))
                        .filter(pair -> pair.length == 2)
                        .collect(Collectors.toMap(p -> p[0].trim(), p -> p[1].trim()));

                summary.setAttributes(attributeMap);
                return summary;
            }).collect(Collectors.toList());

            response.setVariants(variantSummaries);
        } else {
            response.setVariants(Collections.emptyList());
        }
        return response;
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        String categoryName = "-";

        if (product.getCategoryId() != null) {
            Category category = categoryRepository.findById(product.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Kategori tidak ditemukan untuk ID: " + product.getCategoryId()));
            categoryName = category.getName();
        }

        response.setCategoryName(categoryName);
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setHasVariant(product.getHasVariant());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setMinimumStock(product.getMinimumStock());
        response.setDiscountStatus(product.getDiscountStatus());
        response.setDiscountValue(product.getDiscountValue());
        response.setCreateDate(product.getCreateDate());
        Long productStock = stockRepository.sumQuantityByProductId(product.getId());
        response.setTotalStock(productStock != null ? productStock : 0L);
        return response;
    }
    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProductId());
        response.setSku(variant.getSku());
        response.setAttributes(variant.getAttributes());
        response.setPrice(variant.getPrice());
        response.setStock(variant.getStock());
        response.setMinimumStock(variant.getMinimumStock());
        response.setCreateDate(variant.getCreateDate());
        return response;
    }
    private StockSummaryResponse toStockResponse(Stock stock, ProductVariant variant) {
        StockSummaryResponse response = new StockSummaryResponse();

        response.setId(stock.getId());
        response.setProductId(stock.getProductId());
        response.setVariantId(stock.getVariantId());
        response.setSku(variant.getSku());
        response.setQuantity(stock.getQuantity());
        response.setLocation(stock.getLocation());
        response.setUsedQuantity(stock.getUsedQuantity());

        // Jika kamu ingin menampilkan nama varian, pastikan field-nya sesuai
        response.setVariantName(variant.getAttributes()); // Ganti dari getAttributes() ke getName() jika ada

        // Gunakan tanggal dari variant atau stock sesuai yang paling masuk akal
        response.setCreateDate(stock.getCreateDate()); // atau stock.getCreateDate() jika kamu ingin tanggal stok
        response.setUpdateDate(stock.getUpdateDate()); // sama juga di sini

        return response;
    }
    private StockSummaryResponse toStockResponse(Stock stock) {
        StockSummaryResponse response = new StockSummaryResponse();

        response.setId(stock.getId());
        response.setProductId(stock.getProductId());
        response.setVariantId(null); // tidak ada varian
        response.setQuantity(stock.getQuantity());
        response.setLocation(stock.getLocation());
        response.setUsedQuantity(stock.getUsedQuantity());
        response.setVariantName(null); // atau bisa "N/A" jika kamu mau tampilkan string
        response.setCreateDate(stock.getCreateDate());
        response.setUpdateDate(stock.getUpdateDate());

        return response;
    }

    public Map<String, String> parseAttributes(String attrStr) {
        Map<String, String> map = new HashMap<>();
        if (attrStr == null || attrStr.isEmpty()) return map;

        String[] pairs = attrStr.split(","); // escape | because it's a regex special char
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }
        return map;
    }


    public List<ProductCategoryResponse> getUncategorizedProducts(Long shopId) {

        List<Product> products = productRepository.findByShopIdAndCategoryIdIsNullAndDeleteDateIsNull(shopId);

        List<ProductCategoryResponse> result = new ArrayList<>();
        for (Product product : products) {
            ProductCategoryResponse response = new ProductCategoryResponse();
            response.setId(product.getId());
            response.setName(product.getName());
            result.add(response);
        }
        return result;
    }

    public ProductCategoryResponse updateProductCategory(Long productId, Long shopId, Long categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Kategori tidak ditemukan"));

            if (!category.getShopId().equals(shopId)) {
                throw new UserUnauthorizedException("Kategori tidak milik shop Anda");
            }

            product.setCategoryId(categoryId);
        } else {
            product.setCategoryId(null); // hapus kategori
        }
        productRepository.save(product);
        ProductCategoryResponse response = new ProductCategoryResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        return response;
    }
}