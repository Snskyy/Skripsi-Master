package com.inventory.inventory.service;

import com.inventory.inventory.dto.cart.CartRequest;
import com.inventory.inventory.dto.cart.CartResponse;
import com.inventory.inventory.dto.cart.ListCartResponse;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.model.Cart;
import com.inventory.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private SupplierCustomerRepository supplierRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    public CartResponse createCart(CartRequest request, Long userId, Long shopId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setShopId(shopId);
        cart.setProductId(request.getProductId());
        cart.setVariantId(request.getVariantId());
        cart.setQuantity(request.getQuantity());
        cart.setPrice(request.getPrice());
        cart.setType(request.getType());
        cart.setCreateDate(LocalDateTime.now());

        cart = cartRepository.save(cart); // simpan dulu agar dapat ID

        // Bangun response
        CartResponse res = new CartResponse();
        res.setId(cart.getId());
        res.setProductId(cart.getProductId());
        res.setVariantId(cart.getVariantId());
        res.setQuantity(cart.getQuantity());
        res.setPrice(cart.getPrice());
        res.setSubTotal(cart.getQuantity() * cart.getPrice());

        // Ambil info produk
        productRepository.findById(cart.getProductId())
                .ifPresent(p -> res.setProductName(p.getName()));

        // Ambil info varian
        if (cart.getVariantId() != null) {
            productVariantRepository.findById(cart.getVariantId())
                    .ifPresent(v -> {
                        res.setVariantName(v.getAttributes());
                        res.setSku(v.getSku());
                    });
        }

        return res;
    }
//
//    public List<CartResponse> createCarts(List<CartRequest> requests, Long userId, Long shopId) {
////        shopService.shopValidationWithUserId(userId, shopId);
//        List<CartResponse> responses = new ArrayList<>();
//
//        for (CartRequest request : requests) {
//            boolean hasQty = request.getQuantity() != null && request.getQuantity() > 0;
//            boolean hasPrice = request.getPrice() != null && request.getPrice() > 0;
//
//            if (!hasQty && !hasPrice) {
//                continue; // abaikan item
//            }
//
//            if ((hasQty && !hasPrice) || (!hasQty && hasPrice)) {
//                throw new BadRequestException("Quantity dan harga harus diisi bersamaan");
//            }
//
//            // Cek apakah sudah ada cart yang sama (berdasarkan productId, variantId, type, shopId, userId)
//            Optional<Cart> existingCartOpt = cartRepository.findByUserIdAndShopIdAndProductIdAndVariantIdAndType(
//                    userId, shopId, request.getProductId(), request.getVariantId(), request.getType()
//            );
//
//            Cart cart;
//
//            if (existingCartOpt.isPresent()) {
//                cart = existingCartOpt.get();
//                cart.setQuantity(cart.getQuantity() + request.getQuantity()); // Tambah quantity
//                cart.setPrice(request.getPrice()); // Ganti harga dengan yang baru
//            } else {
//                cart = new Cart();
//                cart.setUserId(userId);
//                cart.setShopId(shopId);
//                cart.setProductId(request.getProductId());
//                cart.setVariantId(request.getVariantId());
//                cart.setQuantity(request.getQuantity());
//                cart.setPrice(request.getPrice());
//                cart.setType(request.getType());
//                cart.setCreateDate(LocalDateTime.now());
//            }
//
//            cart = cartRepository.save(cart);
//
//            CartResponse res = new CartResponse();
//
//            res.setId(cart.getId());
//            res.setProductId(cart.getProductId());
//            res.setVariantId(cart.getVariantId());
//            res.setQuantity(cart.getQuantity());
//            res.setPrice(cart.getPrice());
//            res.setSubTotal(cart.getQuantity() * cart.getPrice());
//
//            productRepository.findById(cart.getProductId())
//                    .ifPresent(p -> res.setProductName(p.getName()));
//
//            if (cart.getVariantId() != null) {
//                productVariantRepository.findById(cart.getVariantId())
//                        .ifPresent(v -> {
//                            res.setVariantName(v.getAttributes());
//                            res.setSku(v.getSku());
//                        });
//            }
//
//            responses.add(res);
//        }
//
//        return responses;
//    }

    // pastiin di controller isi type secara manual
    public ListCartResponse getCartByUserShopType(Long userId, Long shopId, String type) {
        List<Cart> items = cartRepository.findByUserIdAndShopIdAndType(userId, shopId, type); // type: "purchase" / "sales"
        List<CartResponse> responses = new ArrayList<>();
        long total = 0L;

        for (Cart cart : items) {

            CartResponse res = new CartResponse();
            res.setId(cart.getId());
            res.setProductId(cart.getProductId());
            res.setVariantId(cart.getVariantId());
            res.setQuantity(cart.getQuantity());
            res.setPrice(cart.getPrice());

            // Ambil nama produk
            productRepository.findById(cart.getProductId()).ifPresent(product -> {
                res.setProductName(product.getName());
            });

            // Ambil data varian (attributes dan sku)
            if (cart.getVariantId() != null) {
                productVariantRepository.findById(cart.getVariantId()).ifPresent(variant -> {
                    res.setVariantName(variant.getAttributes());
                    res.setSku(variant.getSku());
                });
            }

            long subTotal = cart.getQuantity() * cart.getPrice();
            res.setSubTotal(subTotal);
            total += subTotal;

            responses.add(res);
        }

        ListCartResponse result = new ListCartResponse();
        result.setItems(responses);
        result.setTotal(total);
        return result;
    }

    public void updateCartPurchase(Long cartId, CartRequest request, Long shopId) {
//        shopService.shopValidationWithUserId(userId, shopId);
        boolean hasQty = request.getQuantity() != null && request.getQuantity() > 0;
        boolean hasPrice = request.getPrice() != null && request.getPrice() > 0;

        if (!hasQty && !hasPrice) {
            throw new BadRequestException("Tidak ada perubahan: quantity dan price kosong");
        }

        if ((hasQty && !hasPrice) || (!hasQty && hasPrice)) {
            throw new BadRequestException("Mohon isi keduanya: quantity dan harga");
        }
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        // ✅ VALIDASI: Pastikan stok mencukupi
        if ("SALES".equalsIgnoreCase(cart.getType())) {
            long requestedQty = request.getQuantity();

            // Hitung total stok tersedia
            Long totalAvailable;
            if (cart.getVariantId() != null) {
                totalAvailable = stockRepository.sumAvailableQuantityByVariantId(cart.getVariantId());
            } else {
                totalAvailable = stockRepository.sumAvailableQuantityByProductId(cart.getProductId());
            }

            totalAvailable = totalAvailable != null ? totalAvailable : 0L;

            // Kurangi stok yang sudah terpakai oleh cart lain
            List<Cart> otherCarts = cartRepository.findSimilarCartsExcludingId(
                    shopId, cart.getType(), cart.getProductId(), cart.getVariantId(), cartId
            );

            long usedInOtherCarts = otherCarts.stream()
                    .mapToLong(c -> c.getQuantity() != null ? c.getQuantity() : 0)
                    .sum();

            long remainingStock = totalAvailable - usedInOtherCarts;

            if (requestedQty > remainingStock) {
                throw new BadRequestException("Qty melebihi stok tersedia. Maksimum yang bisa diinput: " + remainingStock);
            }
        }
        cart.setQuantity(request.getQuantity());
        cart.setPrice(request.getPrice());
        cartRepository.save(cart);
    }

    public CartResponse deleteCartAndReturn(Long id) {
//        shopService.shopValidationWithUserId(userId, shopId);
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setProductId(cart.getProductId());
        response.setVariantId(cart.getVariantId());
        response.setQuantity(cart.getQuantity());
        response.setPrice(cart.getPrice());
        response.setSubTotal(cart.getQuantity() * cart.getPrice());

        // Isi tambahan (opsional)
        productRepository.findById(cart.getProductId())
                .ifPresent(p -> response.setProductName(p.getName()));
        if (cart.getVariantId() != null) {
            productVariantRepository.findById(cart.getVariantId())
                    .ifPresent(v -> {
                        response.setVariantName(v.getAttributes());
                        response.setSku(v.getSku());
                    });
        }
        cartRepository.deleteById(id);
        return response;
    }

    public void clearCart(Long userId, Long shopId, String type) {
        if (userId == null || shopId == null || type == null || type.isBlank()) {
            throw new IllegalArgumentException("Parameter tidak lengkap untuk clearCart.");
        }
        List<Cart> carts = cartRepository.findByUserIdAndShopIdAndType(userId, shopId, type);
        if (carts == null) {
            throw new RuntimeException("Data cart tidak ditemukan.");
        }
        cartRepository.deleteAll(carts);
    }

}
