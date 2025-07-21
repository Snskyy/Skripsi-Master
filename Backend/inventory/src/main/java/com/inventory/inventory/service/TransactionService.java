package com.inventory.inventory.service;

import com.inventory.inventory.dto.transaction.*;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.model.*;
import com.inventory.inventory.repository.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionHeaderRepository transactionHeaderRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierCustomerRepository supplierCustomerRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Transactional
    public CheckoutResponse checkoutCart(TransactionRequest request, Long userId, Long shopId) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Transaksi harus memiliki minimal satu item.");
        }
        String kasirName = userRepository.findById(userId)
                .map(User::getName)
                .orElse("Kasir");
        Long total = 0L;
        TransactionHeader header = new TransactionHeader();

        header.setUserId(userId);
        header.setCreateBy(kasirName);
        header.setShopId(shopId);
        header.setPlatformId(request.getPlatformId());
        header.setType(request.getType());
        header.setStatus(request.getStatus());
        header.setPaymentMethod(request.getPaymentMethod());
//        header.setTotal(request.getTotal());
        header.setNote(request.getNote());
        header.setCreateDate(LocalDateTime.now());

        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()) {
            if (transactionHeaderRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
                throw new BadRequestException("Nomor invoice sudah digunakan.");
            }

            header.setInvoiceNumber(request.getInvoiceNumber());
        } else {
            // Buat invoice number otomatis, contoh: PUR-12-20250605-001
            String prefix = request.getType().equalsIgnoreCase("PURCHASE") ? "PUR" : "SAL";
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));


            // Hitung jumlah transaksi dari shop itu pada hari itu
            long countToday = transactionHeaderRepository.countByTypeAndShopIdAndDate(
                    request.getType(), shopId, LocalDate.now());

            String generated = String.format("%s-%d%s-%03d", prefix, shopId, today, countToday + 1);
            header.setInvoiceNumber(generated);
        }

        // Nama supplier/customer
        if (request.getSupplierCustomerId() != null) {
            SupplierCustomer sc = supplierCustomerRepository.findById(request.getSupplierCustomerId())
                    .orElseThrow(() -> new NotFoundException("Supplier/Customer tidak ditemukan"));
            header.setSupplierCustomerId(sc.getId());
            header.setSupplierCustomerName(sc.getName());

        } else if (!"CANCELED".equalsIgnoreCase(request.getStatus())) {

            String trimmedName = request.getSupplierCustomerName() != null ? request.getSupplierCustomerName().trim() : "";

            // Jika nama kosong/null → fallback ke default
            if (trimmedName.isEmpty()) {
                SupplierCustomer defaultSC = supplierCustomerRepository
                        .findByShopIdAndTypeAndIsDefaultTrue(shopId, request.getType().equalsIgnoreCase("PURCHASE") ? "SUPPLIER" : "CUSTOMER")
                        .orElseThrow(() -> new NotFoundException("Default supplier/customer tidak ditemukan"));

                header.setSupplierCustomerId(defaultSC.getId());
                header.setSupplierCustomerName(defaultSC.getName());

            } else {
                // Buat supplier/customer baru jika tidak kosong
                header.setSupplierCustomerName(trimmedName);
                SupplierCustomer newSC = new SupplierCustomer();
                newSC.setName(trimmedName);
                newSC.setCreateDate(LocalDateTime.now());
                newSC.setUpdateDate(LocalDateTime.now());
                newSC.setShopId(shopId);
                newSC.setType(request.getType().equalsIgnoreCase("PURCHASE") ? "SUPPLIER" : "CUSTOMER");
                newSC.setLastTransactionDate(LocalDateTime.now());
                SupplierCustomer saved = supplierCustomerRepository.save(newSC);

                header.setSupplierCustomerId(saved.getId());
            }

        } else {
            // Jika status CANCELED, tetap isi namanya meskipun mungkin kosong
            header.setSupplierCustomerName(request.getSupplierCustomerName());
        }

        for (TransactionDetailRequest d : request.getItems()) {
            if (d.getQuantity() == null  || d.getQuantity() < 0 ) {
                throw new BadRequestException("Quantity harus lebih dari 0");
            }
        }



        TransactionHeader saveHeader = transactionHeaderRepository.save(header);


        Map<String, Long> detailIdMap = new HashMap<>();
        // Simpan detail
        for (TransactionDetailRequest d : request.getItems()) {
            TransactionDetail detail = new TransactionDetail();
            String productName = productRepository.findById(d.getProductId()).orElseThrow(() -> new NotFoundException("Product tidak ditemukan")).getName();
            if (d.getVariantId() != null) {
                ProductVariant productVariant = productVariantRepository.findById(d.getVariantId()).orElseThrow(() -> new NotFoundException("Variant tidak ditemukan"));
                detail.setVariantId(productVariant.getId());
                detail.setSku(productVariant.getSku());
                detail.setVariantAttributes(productVariant.getAttributes());
            }
            detail.setProductName(productName);
            detail.setTransactionId(saveHeader.getId());
            detail.setProductId(d.getProductId());

            detail.setQuantity(d.getQuantity());
            detail.setPrice(d.getPrice());
            detail.setSubTotal(d.getPrice() * d.getQuantity());
            detail.setCreateDate(LocalDateTime.now());
            detail.setUpdateDate(LocalDateTime.now());
            total += d.getPrice() * d.getQuantity();
            TransactionDetail savedDetail = transactionDetailRepository.save(detail);
            String key = d.getProductId() + "-" + (d.getVariantId() != null ? d.getVariantId() : "no-var");
            detailIdMap.put(key, savedDetail.getId());
        }
        saveHeader.setTotal(total);
        saveHeader.setUpdateDate(LocalDateTime.now());
        transactionHeaderRepository.save(saveHeader);

        // ❌ Hapus cart
        String type = request.getType().toUpperCase();
        cartRepository.deleteByUserIdAndShopIdAndType(userId, shopId, type);

        // ❗ Kurangi stok jika bukan pending/canceled
        if ("SALES".equals(type)
                && !"PENDING".equalsIgnoreCase(request.getStatus())
                && !"CANCELED".equalsIgnoreCase(request.getStatus())) {

            for (TransactionDetailRequest d : request.getItems()) {
                long qtyToReduce = d.getQuantity();

                List<Stock> stockList = (d.getVariantId() != null)
                        ? stockRepository.findByVariantIdOrderByCreateDateAsc(d.getVariantId())
                        : stockRepository.findByProductIdOrderByCreateDateAsc(d.getProductId());

                String key = d.getProductId() + "-" + (d.getVariantId() != null ? d.getVariantId() : "no-var");
                Long detailId = detailIdMap.get(key);

                for (Stock stock : stockList) {
                    long usedQty = stock.getUsedQuantity() != null ? stock.getUsedQuantity() : 0;
                    long available = stock.getQuantity() - usedQty;

                    if (available <= 0) continue;

                    long toUse = Math.min(qtyToReduce, available);
                    stock.setUsedQuantity(usedQty + toUse);
                    stock.setUpdateDate(LocalDateTime.now());

//                    if ((stock.getQuantity() - stock.getUsedQuantity()) == 0) {
//                        stock.setDisabled(1L);
//                        stock.setDeleteDate(LocalDate.now());
//                    }

                    stockRepository.save(stock);

                    // ✅ Catat history OUT
                    StockHistory history = new StockHistory();
                    history.setProductId(stock.getProductId());
                    history.setVariantId(stock.getVariantId());
                    history.setStockId(stock.getId());
                    history.setStockInOut("OUT");
                    history.setStockType("SALE_PAID");
                    history.setNote(request.getNote());
                    history.setQuantity(toUse);
                    history.setLocation(stock.getLocation());
                    history.setCreateDate(LocalDateTime.now());
                    history.setCreatedBy(userId);
                    history.setTransactionDetailId(detailId);
                    stockHistoryRepository.save(history);

                    qtyToReduce -= toUse;
                    if (qtyToReduce == 0) break;
                }

                if (qtyToReduce > 0) {
                    throw new BadRequestException("Stok tidak mencukupi untuk produk: " + d.getProductId());
                }

                // ✅ Update ringkasan stok
                if (d.getVariantId() != null) {
                    Long totalVariantStock = stockRepository.sumAvailableQuantityByVariantId(d.getVariantId());
                    ProductVariant variant = productVariantRepository.findById(d.getVariantId()).orElseThrow();
                    variant.setStock(totalVariantStock);
                    variant.setUpdateDate(LocalDateTime.now());
                    productVariantRepository.save(variant);
                }

                Long totalProductStock = stockRepository.sumAvailableQuantityByProductId(d.getProductId());
                Product product = productRepository.findById(d.getProductId()).orElseThrow();
                product.setStock(totalProductStock);
                product.setUpdateDate(LocalDateTime.now());
                productRepository.save(product);
            }

        }


        CheckoutResponse response = new CheckoutResponse();
        response.setInvoiceNumber(saveHeader.getInvoiceNumber());
        response.setSupplierCustomerId(saveHeader.getSupplierCustomerId());
        response.setSupplierCustomerName(saveHeader.getSupplierCustomerName());

        return response;
    }

    @Transactional
    public List<TransactionResponse> getTransactions(Long shopId, String status) {
        // Validasi shop
//        Shop shop = shopRepository.findById(shopId)
//                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));
//
//        if (!shop.getUserId().equals(userId)) {
//            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
//        }

        // Ambil transaksi, dengan atau tanpa filter status
        List<TransactionHeader> transactions;
        if (status != null && !status.isBlank()) {
            transactions = transactionHeaderRepository.findByShopIdAndStatusOrderByCreateDateDesc(shopId, status.toUpperCase());
        } else {
            transactions = transactionHeaderRepository.findByShopIdOrderByCreateDateDesc(shopId);
        }

        // Mapping ke DTO
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public String  updateTransactionStatus(Long transactionId, String newStatus, String note, Long userId) {
        TransactionHeader header = transactionHeaderRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction tidak ditemukan"));

        String type = header.getType().toUpperCase();
        String currentStatus = header.getStatus().toUpperCase();
        newStatus = newStatus.toUpperCase();

        // Definisi transisi status yang diperbolehkan
        Map<String, List<String>> transitions = new HashMap<>();

        if ("PURCHASE".equals(type)) {
            transitions.put("PENDING", List.of("PAID", "CANCELED"));
            transitions.put("PAID", List.of("RECEIVED", "FINAL", "CANCELED"));
            transitions.put("RECEIVED", List.of("FINAL"));
            transitions.put("FINAL", List.of());
            transitions.put("CANCELED", List.of());
        } else if ("SALES".equals(type)) {
            transitions.put("PENDING", List.of("PAID", "CANCELED"));
            transitions.put("PAID", List.of("FINAL", "CANCELED"));
            transitions.put("FINAL", List.of());
            transitions.put("CANCELED", List.of());
        } else {
            throw new BadRequestException("Invalid transaction type: " + type);
        }

        // Validasi status sekarang
        if (!transitions.containsKey(currentStatus)) {
            throw new BadRequestException("Unknown current status: " + currentStatus);
        }

        // Validasi transisi ke status baru
        if (!transitions.get(currentStatus).contains(newStatus)) {
            throw new BadRequestException("Cannot change status from " + currentStatus + " to " + newStatus);
        }

        if ("SALES".equals(type)) {
            // Hanya kurangi stok saat transisi ke PAID
            if (!"PAID".equals(currentStatus) && "PAID".equals(newStatus)) {
                List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(header.getId());
                for (TransactionDetail detail : details) {
                    deductStockForSales(detail, header.getUserId());
                }
            }
            // 🔁 Rollback stok saat PAID → CANCELED
            if ("PAID".equals(currentStatus) && "CANCELED".equals(newStatus)) {
                List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(header.getId());
                for (TransactionDetail detail : details) {
                    rollbackStockFromHistory(detail.getId(), userId);
                }
            }
        }
        // Simpan note jika ada
        if (note != null && !note.isBlank()) {
            header.setNote(note.trim());
        }


        header.setStatus(newStatus);
        header.setUpdateDate(LocalDateTime.now());
        transactionHeaderRepository.save(header);
        return newStatus;
    }
    public void deductStockForSales(TransactionDetail detail, Long userId) {
        Long remaining = detail.getQuantity();
        Long productId = detail.getProductId();
        Long variantId = detail.getVariantId(); // null if not using variants

        List<Stock> entries;

        if (variantId != null) {
            entries = stockRepository.findByVariantIdOrderByCreateDateAsc(variantId);
        } else {
            entries = stockRepository.findByProductIdOrderByCreateDateAsc(productId);
        }

        for (Stock entry : entries) {
            long used = entry.getUsedQuantity() != null ? entry.getUsedQuantity() : 0;
            long available = entry.getQuantity() - used;

            if (available <= 0) continue;

            long toUse = Math.min(available, remaining);

            entry.setUsedQuantity(used + toUse);
            stockRepository.save(entry);

            // Record stock movement
            stockService.recordStockHistory(entry, "OUT", "SALE_PAID", toUse, userId, detail.getId(), null);

            remaining -= toUse;
            if (remaining == 0) break;
        }

        if (remaining > 0) {
            // Ambil nama produk dan varian (jika ada)
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Produk tidak ditemukan"));

            String namaProduk = product.getName();
            String keterangan = namaProduk;

            if (variantId != null) {
                ProductVariant variant = productVariantRepository.findById(variantId)
                        .orElse(null);
                if (variant != null && variant.getAttributes() != null) {
                    keterangan += " (" + variant.getAttributes() + ")";
                }
            }

            throw new RuntimeException("Stok tidak mencukupi untuk produk: " + keterangan);
        }

        // ✅ Update Product and/or Variant stock summary
        reduceStockSummary(productId, variantId, detail.getQuantity());
    }


    @Transactional
    private void reduceStockSummary(Long productId, Long variantId, Long quantity) {
        String namaProduk = "-";
        String namaVarian = null;

        if (variantId != null) {
            // Kurangi stok varian
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Varian tidak ditemukan"));

            // Ambil nama produk berdasarkan productId dari varian
            Product product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

            namaVarian = variant.getAttributes(); // misalnya: "Ukuran: M, Warna: Merah"
            namaProduk = product.getName(); // Nama produk yang terkait dengan varian

            long stokBaruVarian = variant.getStock() - quantity;
            if (stokBaruVarian < 0) {
                throw new IllegalStateException(
                        String.format("Stok varian tidak mencukupi untuk %s (%s). Tersedia: %d, Diminta: %d",
                                namaProduk,
                                namaVarian != null ? namaVarian : "-",
                                variant.getStock(),
                                quantity
                        )
                );
            }

            variant.setStock(stokBaruVarian);
            variant.setUpdateDate(LocalDateTime.now());
            productVariantRepository.save(variant);
        }

        // Kurangi total stok produk
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        if (variantId == null) {
            namaProduk = product.getName(); // Jika produk tidak menggunakan varian
        }

        long stokBaruProduk = product.getStock() - quantity;
        if (stokBaruProduk < 0) {
            throw new IllegalStateException(
                    String.format("Stok total produk tidak mencukupi untuk %s. Tersedia: %d, Diminta: %d",
                            namaProduk,
                            product.getStock(),
                            quantity
                    )
            );
        }

        product.setStock(stokBaruProduk);
        product.setUpdateDate(LocalDateTime.now());
        productRepository.save(product);
    }


    @Transactional
    public void rollbackStockFromHistory(Long transactionDetailId, Long userId) {
        List<StockHistory> histories = stockHistoryRepository.findByTransactionDetailId(transactionDetailId);

        for (StockHistory history : histories) {
            if (!"OUT".equalsIgnoreCase(history.getStockInOut())) continue;

            Stock stock = stockRepository.findById(history.getStockId())
                    .orElseThrow(() -> new NotFoundException("Stock tidak ditemukan"));

            Long used = stock.getUsedQuantity() == null ? 0 : stock.getUsedQuantity();
            stock.setUsedQuantity(Math.max(0, used - history.getQuantity()));
            stock.setUpdateDate(LocalDateTime.now());

            if (stock.getQuantity() - stock.getUsedQuantity() > 0) {
                stock.setDeleteDate(null); // stok aktif kembali
            }

            stockRepository.save(stock);

            // ✅ Simpan log pengembalian
            StockHistory rollback = new StockHistory();
            rollback.setProductId(stock.getProductId());
            rollback.setVariantId(stock.getVariantId());
            rollback.setStockId(stock.getId());
            rollback.setStockInOut("IN");
            rollback.setStockType("SALE_CANCELED");
            rollback.setQuantity(history.getQuantity());
            rollback.setLocation(stock.getLocation());
            rollback.setCreateDate(LocalDateTime.now());
            rollback.setCreatedBy(userId);
            rollback.setTransactionDetailId(transactionDetailId);
            stockHistoryRepository.save(rollback);
        }

        // ⏫ Update ringkasan stok setelah rollback
        updateTotalStockBasedOnRepository(transactionDetailId);
    }


    public void updateTotalStockBasedOnRepository(Long transactionDetailId) {
        TransactionDetail detail = transactionDetailRepository.findById(transactionDetailId)
                .orElseThrow(() -> new NotFoundException("Detail tidak ditemukan"));

        if (detail.getVariantId() != null) {
            Long totalVariant = stockRepository.sumAvailableQuantityByVariantId(detail.getVariantId());
            ProductVariant v = productVariantRepository.findById(detail.getVariantId()).orElseThrow();
            v.setStock(totalVariant);
            v.setUpdateDate(LocalDateTime.now());
            productVariantRepository.save(v);
        }

        Long totalProduct = stockRepository.sumAvailableQuantityByProductId(detail.getProductId());
        Product p = productRepository.findById(detail.getProductId()).orElseThrow();
        p.setStock(totalProduct);
        p.setUpdateDate(LocalDateTime.now());
        productRepository.save(p);
    }



    public byte[] generateInvoicePdf(String invoiceNumber) {

        TransactionHeader header = transactionHeaderRepository
                .findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new NotFoundException("Transaksi tidak ditemukan"));

        List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(header.getId());
        Shop shop = shopRepository.findById(header.getShopId())
                .orElseThrow(() -> new NotFoundException("Toko tidak ditemukan"));

        String kasirName = header.getCreateBy();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Header toko
            document.add(new Paragraph(shop.getName()).setBold().setFontSize(16));
            document.add(new Paragraph(shop.getAddress()).setFontSize(10));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("INVOICE").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(16));
            document.add(new LineSeparator(new SolidLine()));

            // Informasi transaksi
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            infoTable.addCell(noBorder("No Invoice: " + header.getInvoiceNumber()));
            infoTable.addCell(noBorder("Tanggal: " + header.getCreateDate().toLocalDate()));
            infoTable.addCell(noBorder("Status: " + header.getStatus()));
            infoTable.addCell(noBorder("Pembayaran: " + header.getPaymentMethod()));
            infoTable.addCell(noBorder(
                    header.getType().equalsIgnoreCase("PURCHASE") ?
                            "Supplier: " + header.getSupplierCustomerName() :
                            "Customer: " + header.getSupplierCustomerName()
            ));
            infoTable.addCell(noBorder("Kasir: " + kasirName));
            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Tabel produk
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 1, 1, 1}))
                    .useAllAvailableWidth();
            String[] headers = {"Produk", "Varian", "Qty", "Harga", "Subtotal"};
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }

            long total = 0;
            for (TransactionDetail d : details) {
                String productName = productRepository.findById(d.getProductId())
                        .map(Product::getName).orElse("Produk");

                table.addCell(new Cell().add(new Paragraph(productName)));

                // Format varian bullet list
                com.itextpdf.layout.element.List variantList = new com.itextpdf.layout.element.List()
                        .setFontSize(9)
                        .setListSymbol("\u2022 ");
                if (d.getVariantAttributes() != null && !d.getVariantAttributes().isBlank()) {
                    String[] attrs = d.getVariantAttributes().split(",");
                    for (String attr : attrs) {
                        String[] parts = attr.split(":");
                        if (parts.length == 2) {
                            variantList.add(new ListItem(parts[0].trim() + ": " + parts[1].trim()));
                        }
                    }
                } else {
                    variantList.add(new ListItem("-"));
                }
                table.addCell(new Cell().add(variantList));


                table.addCell(new Cell().add(new Paragraph(String.valueOf(d.getQuantity()))));
                table.addCell(new Cell().add(formatHarga(d.getPrice())));
                table.addCell(new Cell().add(formatHarga(d.getSubTotal())));
                total += d.getSubTotal();
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Total akhir
            Paragraph totalText = new Paragraph()
                    .add("Total: ").setBold()
                    .add(formatHarga(total))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12);
            document.add(totalText);

        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat PDF invoice", e);
        }

        return baos.toByteArray();
    }

    private Cell noBorder(String text) {
        return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setFontSize(10);
    }
    private Paragraph formatHarga(long value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        nf.setMaximumFractionDigits(0); // menghilangkan .00
        nf.setMinimumFractionDigits(0);

        String formatted = nf.format(value).replace("Rp", "Rp ");
        return new Paragraph(formatted)
                .setFontSize(10); // lebih kecil, tidak terlalu besar
    }

    private TransactionResponse mapToResponse(TransactionHeader transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setSupplierCustomerName(transaction.getSupplierCustomerName());
        response.setNote(transaction.getNote());
        response.setTotal(transaction.getTotal());
        response.setStatus(transaction.getStatus());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setPlatformId(transaction.getPlatformId());
        response.setPlatformName(transaction.getPlatformName());
        response.setSupplierCustomerId(transaction.getSupplierCustomerId());
        response.setUserId(transaction.getUserId());
        response.setCreateBy(transaction.getCreateBy());
        response.setShopId(transaction.getShopId());
        response.setInvoiceNumber(transaction.getInvoiceNumber());
        response.setCreateDate(transaction.getCreateDate());
        response.setUpdateDate(transaction.getUpdateDate());
        response.setDeleteDate(transaction.getDeleteDate());

        // Ambil detail transaksi
        List<TransactionDetailResponse> details = transactionDetailRepository.findByTransactionId(transaction.getId())
                .stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
        response.setItems(details);

        return response;
    }

    private TransactionDetailResponse mapToDetailResponse(TransactionDetail detail) {
        TransactionDetailResponse dto = new TransactionDetailResponse();
        dto.setId(detail.getId());
        dto.setTransactionId(detail.getTransactionId());
        dto.setProductId(detail.getProductId());
        dto.setProductName(detail.getProductName());
        dto.setVariantId(detail.getVariantId());
        dto.setSKU(detail.getSku());
        dto.setVariantAttributes(detail.getVariantAttributes());
        dto.setQuantity(detail.getQuantity());
        dto.setPrice(detail.getPrice());
        dto.setSubTotal(detail.getSubTotal());
        dto.setFlag(detail.getFlag());
        dto.setCreateDate(detail.getCreateDate());
        dto.setUpdateDate(detail.getUpdateDate());
        dto.setDeleteDate(detail.getDeleteDate());
        return dto;
    }

}
