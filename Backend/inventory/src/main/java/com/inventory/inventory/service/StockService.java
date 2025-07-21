package com.inventory.inventory.service;

import com.inventory.inventory.dto.product.MinimumStockResponse;
import com.inventory.inventory.dto.stock.*;
import com.inventory.inventory.exception.*;
import com.inventory.inventory.model.*;
import com.inventory.inventory.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;


    @Autowired
    private TransactionHeaderRepository transactionHeaderRepository;

    public List<StockEntryDetailResponse> getAllStockDetails(long shopId) {
        List<Stock> stocks = stockRepository.findAllByShopIdAndDeleteDateIsNull(shopId);
        return stocks.stream().map(this::toDetailResponse).collect(Collectors.toList());
    }

    public StockEntryDetailResponse createStockEntry(StockEntryRequest request, Long userId, Long shopId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));
        // 🔒 Validasi wajib untuk produk dengan varian
        if (Boolean.TRUE.equals(product.getHasVariant())) {
            if (request.getVariantId() == null) {
                throw new BadRequestException("Produk ini memiliki varian. Mohon pilih varian terlebih dahulu.");
            }

            // Validasi varian sesuai product
            ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));

            if (!variant.getProductId().equals(product.getId())) {
                throw new BadRequestException("Varian tidak sesuai dengan produk yang dipilih.");
            }
        }

        // 🔒 Validasi lokasi tidak kosong
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new BadRequestException("Lokasi tidak boleh kosong.");
        }

        // 🔒 Validasi quantity harus ada dan > 0
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new BadRequestException("Jumlah stok harus diisi dan tidak boleh kurang dari 0.");
        }
        ProductVariant variant = null;
        String sku = null;
        String attributes = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));
            sku = variant.getSku();
            attributes = formatAttributes(variant.getAttributes());
        }

        // Simpan stok baru
        Stock stock = new Stock();
        stock.setProductId(request.getProductId());
        stock.setShopId(shopId);
        stock.setUsedQuantity(0L);
        stock.setVariantId(request.getVariantId());
        stock.setLocation(request.getLocation());
        stock.setQuantity(request.getQuantity());
        stock.setDisabled(0L); // ootmatis baru buat jadi ga perlu set diable
        stock.setCreateDate(LocalDateTime.now());
        stock.setUpdateDate(LocalDateTime.now());
        stockRepository.save(stock);

        recordStockHistory(
                stock,
                "IN",                          // stockInOut
                "CREATE_STOCK",                // stockType
                request.getQuantity(),        // jumlah awal
                userId,
                null,                         // transactionDetailId tidak ada
                request.getNote() != null && !request.getNote().isBlank()
                        ? request.getNote()
                        : "Penambahan stok awal ke lokasi " + stock.getLocation()
        );

        // ✅ Recalculate stock produk
        Long productStock = stockRepository.sumQuantityByProductId(product.getId());
        product.setStock(productStock != null ? productStock : 0);
        productRepository.save(product);
        StockEntryDetailResponse response = new StockEntryDetailResponse();
        // ✅ Recalculate stock varian (jika ada)
        if (variant != null) {
            Long variantStock = stockRepository.sumQuantityByVariantId(variant.getId());
            variant.setStock(variantStock != null ? variantStock : 0);
            response.setVariantQuantity(variantStock);
            productVariantRepository.save(variant);
        }
        response.setStockId(stock.getId());
        response.setProductName(product.getName());
        response.setSku(sku);
        response.setAttributes(attributes);
        response.setQuantity(stock.getQuantity());
        response.setLocation(stock.getLocation());
        response.setCreateDate(stock.getCreateDate());
        response.setUpdateDate(stock.getUpdateDate());
        response.setProductQuantity(productStock);
        return response;
    }

    private String formatAttributes(String attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        return Arrays.stream(attributes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public List<Stock> getStockByProductId(Long productId) {
        return stockRepository.findActiveByProductId(productId);
    }

    public List<Stock> getStockByVariantId(Long variantId) {
        return stockRepository.findActiveByVariantId(variantId);
    }

    public StockEntryDetailResponse updateStock(Long userId, Long stockId, StockEntryRequest request) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Stock entry not found"));

        if (request.getLocation() == null ||request.getLocation().equalsIgnoreCase("")){
            throw new BadRequestException("Lokasi harus harus diisii");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new BadRequestException("Jumlah stok harus diisi dan tidak boleh kurang dari 0.");
        }

        Long oldQty = stock.getQuantity();
        boolean wasDisabled = stock.getDisabled() != null && stock.getDisabled().equals(1L);
        boolean isDisabling = request.getDisable() != null && request.getDisable().equals(1L);
        boolean isEnabling = wasDisabled && (request.getDisable() != null && request.getDisable().equals(0L));
        stock.setQuantity(request.getQuantity());
        stock.setLocation(request.getLocation());
        stock.setDisabled(request.getDisable());
        stock.setUpdateDate(LocalDateTime.now());

        // Update product stock
        Product product = productRepository.findById(stock.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));
        long activeProductStock = 0L;
        for (Stock activeStock : stockRepository.findByProductIdAndDisabled(product.getId(), 0)) {
            if (activeStock.getQuantity() != null) {
                activeProductStock += activeStock.getQuantity();
            }
        }
        product.setStock(activeProductStock);
        productRepository.save(product);

        // Update variant stock if applicable
        Long variantQuantity = null;
        if (stock.getVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(stock.getVariantId())
                    .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));
            long activeVariantStock = 0L;
            for (Stock activeVariantStockEntry : stockRepository.findByVariantIdAndDisabled(stock.getVariantId(), 0)) {
                if (activeVariantStockEntry.getQuantity() != null) {
                    activeVariantStock += activeVariantStockEntry.getQuantity();
                }
            }
            variant.setStock(activeVariantStock);
            productVariantRepository.save(variant);
            variantQuantity = activeVariantStock;
        }

        Stock updatedStock = stockRepository.save(stock);
        if (isDisabling) {
            recordStockHistory(updatedStock,"OUT","DISABLE_STOCK", updatedStock.getQuantity(), userId, null, "Menonaktifkan stok lokasi " + updatedStock.getLocation());
        }else if (isEnabling) {
            recordStockHistory(updatedStock, "IN", "ENABLE_STOCK", updatedStock.getQuantity(), userId, null, "Mengaktifkan kembali stok lokasi " + updatedStock.getLocation());
        }
        if (!oldQty.equals(request.getQuantity())) {
            long diff = request.getQuantity() - oldQty;
            String direction = diff > 0 ? "IN" : "OUT";
            recordStockHistory(updatedStock, direction, "MANUAL_ADJUSTMENT", Math.abs(diff), userId, null, request.getNote() != null && !request.getNote().isBlank() ? request.getNote() : "Penyesuaian manual jumlah stok dari " + oldQty + " ke " + request.getQuantity()
            );
        }
        // Build response
        StockEntryDetailResponse response = new StockEntryDetailResponse();
        response.setStockId(stockId);
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setQuantity(updatedStock.getQuantity());
        response.setProductQuantity(product.getStock());
        response.setLocation(updatedStock.getLocation());
        response.setDisabled(updatedStock.getDisabled());
        response.setCreateDate(updatedStock.getCreateDate());
        response.setUpdateDate(updatedStock.getUpdateDate());
        response.setUsedQuantity(updatedStock.getUsedQuantity());

        // Set variant info if available
        if (stock.getVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(stock.getVariantId())
                    .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));
            response.setVariantId(variant.getId());
            response.setSku(variant.getSku());
            response.setAttributes(formatAttributes(variant.getAttributes()));
            response.setVariantQuantity(variantQuantity != null ? variantQuantity : variant.getStock());
//            response.setUsedQuantity(stock.getUsedQuantity());
        }

        return response;
    }

    public void softDeleteStockById(Long userId, Long stockId, String note) {

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Stok tidak ditemukan"));
        Product product = productRepository.findById(stock.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));

        // Soft delete stock
        stock.setDeleteDate(LocalDateTime.now());
        stockRepository.save(stock);

        recordStockHistory(
                stock,
                "OUT",                         // stockInOut
                "DELETE_STOCK",                // stockType
                stock.getQuantity(),           // quantity dikeluarkan
                userId,
                null,                          // no transactionDetailId
                (note != null && !note.trim().isEmpty()) ? note : "Penghapusan stok"
        );
        // Recalculate product stock
        Long productStock = stockRepository.sumQuantityByProductId(product.getId());
        product.setStock(productStock != null ? productStock : 0);
        productRepository.save(product);

        // Recalculate variant stock if applicable
        if (stock.getVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(stock.getVariantId())
                    .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));

            Long variantStock = stockRepository.sumQuantityByVariantId(stock.getVariantId());
            variant.setStock(variantStock != null ? variantStock : 0);
            productVariantRepository.save(variant);
        }
    }

    private StockEntryDetailResponse toDetailResponse(Stock stock) {
        StockEntryDetailResponse dto = new StockEntryDetailResponse();
        dto.setStockId(stock.getId());
        dto.setQuantity(stock.getQuantity());
        dto.setProductId(stock.getProductId());
        dto.setVariantId(stock.getVariantId());
        dto.setLocation(stock.getLocation());
        dto.setCreateDate(stock.getCreateDate());
        dto.setUpdateDate(stock.getUpdateDate());
        dto.setUsedQuantity(stock.getUsedQuantity());
        dto.setDisabled(stock.getDisabled());

        Product p = productRepository.findById(stock.getProductId()).orElse(null);
        dto.setProductName(p.getName());
        dto.setMinimumStock(p.getMinimumStock());
        dto.setProductQuantity(p.getStock());
        if (stock.getVariantId() != null) {
            ProductVariant v = productVariantRepository.findById(stock.getVariantId()).orElse(null);
            dto.setSku(v.getSku());
            dto.setAttributes(v.getAttributes());
            dto.setMinimumStock(v.getMinimumStock());
            dto.setVariantQuantity(v.getStock());
        }

        return dto;
    }

    public void recordStockHistory(Stock stock, String stockInOut, String stockType, Long quantity, Long userId,  Long transactionDetailId, String note) {
        StockHistory history = new StockHistory();
        history.setProductId(stock.getProductId());
        history.setVariantId(stock.getVariantId());
        history.setShopId(stock.getShopId());
        history.setStockId(stock.getId());
        history.setStockInOut(stockInOut); // "IN", "OUT", "ADJUST"
        history.setStockType(stockType);   // e.g. "MANUAL_ADJUSTMENT", "SALE", etc.
        history.setQuantity(quantity);
        history.setLocation(stock.getLocation());
        history.setTransactionDetailId(transactionDetailId); // ✅ tambahan
        history.setCreateDate(LocalDateTime.now());
        history.setCreatedBy(userId);
        history.setNote(note);

        stockHistoryRepository.save(history);
    }

    @Transactional
    public List<AssignStockResponse> assignStock(AssignStockRequest request, Long userId, Long shopId) {
        TransactionDetail transactionDetail = transactionDetailRepository.findById(request.getTransactionDetailId())
                .orElseThrow(() -> new NotFoundException("Transaction Detail tidak ditemukan"));

        // 3. Ambil produk
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));

        // 4. Ambil varian (jika ada)
        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new VariantNotFoundException("Varian tidak ditemukan"));

            if (!variant.getProductId().equals(product.getId())) {
                throw new BadRequestException("Varian tidak sesuai dengan produk.");
            }
        }


        // Validasi lokasi dan quantity
        for (AssignStockRequest.LocationQuantity locationEntries : request.getLocations()) {
            if (locationEntries.getLocation() == null || locationEntries.getLocation().trim().isEmpty()) {
                throw new BadRequestException("Lokasi tidak boleh kosong.");
            }
            if (locationEntries.getQuantity() == null || locationEntries.getQuantity() <= 0) {
                throw new BadRequestException("Quantity tidak boleh kosong atau kurang dari 1.");
            }
        }

        // 5. Tandai transaction detail sebagai "stocked"
        transactionDetail.setFlag("stocked");
        transactionDetailRepository.save(transactionDetail);

        List<AssignStockResponse.LocationEntry> locationEntries = new ArrayList<>();
        for (AssignStockRequest.LocationQuantity loc : request.getLocations()) {
            Stock stock = new Stock();
            stock.setProductId(product.getId());
            stock.setVariantId(variant != null ? variant.getId() : null);
            stock.setLocation(loc.getLocation());
            stock.setQuantity(loc.getQuantity());
            stock.setUsedQuantity(0L);
            stock.setShopId(shopId);
            stock.setDisabled(0L);
            stock.setCreateDate(LocalDateTime.now());
            stock.setUpdateDate(LocalDateTime.now());
            stockRepository.save(stock);

            // ✅ Tambahkan history stok
            recordStockHistory(
                    stock,
                    "IN", // stockInOut
                    "ASSIGN_STOCK", // stockType
                    loc.getQuantity(),
                    userId,
                    transactionDetail.getId(),
                    "Assign stok awal ke lokasi " + loc.getLocation()
            );
            // Tambahkan stockId ke response
            AssignStockResponse.LocationEntry responseLoc = new AssignStockResponse.LocationEntry();
            responseLoc.setStockId(stock.getId()); // <- ambil ID dari entity yg disave
            responseLoc.setLocation(loc.getLocation());
            responseLoc.setQuantity(loc.getQuantity());
            locationEntries.add(responseLoc);
        }

        // 7. Hitung ulang dan update stok produk & varian
        Long newProductStock = stockRepository.sumQuantityByProductId(product.getId());
        product.setStock(newProductStock != null ? newProductStock : 0);
        productRepository.save(product);

        if (variant != null) {
            Long newVariantStock = stockRepository.sumQuantityByVariantId(variant.getId());
            variant.setStock(newVariantStock != null ? newVariantStock : 0);
            productVariantRepository.save(variant);
        }

        // 8. Siapkan response
        AssignStockResponse response = new AssignStockResponse();
        response.setTransactionDetailId(transactionDetail.getId());
        response.setTransactionId(transactionDetail.getTransactionId());
        response.setProductId(product.getId());
        response.setVariantId(variant != null ? variant.getId() : null);
        response.setProductName(product.getName());
        response.setSku(variant != null ? variant.getSku() : null);
        response.setVariantAttributes(variant != null ? formatAttributes(variant.getAttributes()) : null);
        response.setStocked(true);
        response.setProductQuantity(product.getStock());
        response.setVariantQuantity(variant != null ? variant.getStock() : null);
        response.setCreateDate(LocalDateTime.now());
        response.setUpdateDate(LocalDateTime.now());
        response.setDisabled(0L);
        response.setLocations(locationEntries);
// 9. Cek apakah semua transaction detail sudah di-stock
        Long transactionId = transactionDetail.getTransactionId();
        List<TransactionDetail> allDetails = transactionDetailRepository.findByTransactionId(transactionId);

        boolean allStocked = allDetails.stream()
                .allMatch(td -> "stocked".equalsIgnoreCase(td.getFlag()));

        if (allStocked) {
            TransactionHeader header = transactionHeaderRepository.findById(transactionId)
                    .orElseThrow(() -> new NotFoundException("Transaction Header tidak ditemukan"));
            header.setStatus("FINAL");
            transactionHeaderRepository.save(header);
        }
        return List.of(response);
    }

    public MinimumStockResponse updateMinimumStockByProduct(Long productId, Long variantId, Long newMin) {
        if (variantId != null) {
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new NotFoundException("Varian tidak ditemukan"));
            variant.setMinimumStock(newMin);
            productVariantRepository.save(variant);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Produk tidak ditemukan"));
            product.setMinimumStock(newMin);
            productRepository.save(product);
        }

        MinimumStockResponse response = new MinimumStockResponse();
        response.setProductId(productId);
        response.setVariantId(variantId);
        response.setNewMinimumStock(newMin);
        return response;
    }


}



