package com.inventory.inventory.service;

import com.inventory.inventory.dto.shop.ShopResponse;
import com.inventory.inventory.dto.shop.ShopUpdateRequest;
import com.inventory.inventory.dto.shop.ShopUserNameResponse;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.ProductNotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.User;
import com.inventory.inventory.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {
    @Autowired
    ShopRepository shopRepository;

    public ShopResponse getShop(long shopId, Long userId){
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));

        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
        }

        ShopResponse shopData = new ShopResponse();
        shopData.setName(shop.getName());
        shopData.setPhone(shop.getPhone());
        shopData.setAddress(shop.getAddress());
        shopData.setEmail(shop.getEmail());
        return shopData;
    }

    public ShopResponse updateShop(Long shopId, ShopUpdateRequest request, Long userId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new NotFoundException("Shop not found"));

        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Nama tidak boleh kosong");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Nomor telepon tidak boleh kosong");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new BadRequestException("Alamat tidak boleh kosong");
        }
        // ✳️ Validasi tidak ada perubahan
        boolean isSame =
                request.getName().trim().equals(shop.getName()) &&
                        request.getPhone().trim().equals(shop.getPhone()) &&
                        request.getAddress().trim().equals(shop.getAddress()) &&
                        request.getEmail().trim().equals(shop.getEmail());

        if (isSame) {
            throw new BadRequestException("Tidak ada perubahan data untuk disimpan.");
        }
        shop.setName(request.getName());
        shop.setPhone(request.getPhone());
        shop.setAddress(request.getAddress());
        shop.setEmail(request.getEmail());
        shop.setUpdateDate(LocalDateTime.now());
        shopRepository.save(shop); // Save the updated shop

        return getShop(shopId,userId);
    }

    public List<ShopResponse> getShopsByUserId(User user) {
        List<Shop> shops = shopRepository.findByUserId(user.getId());
        List<ShopResponse> responses = new ArrayList<>();

        for (Shop data : shops) {

            ShopResponse shop = new ShopResponse();
            shop.setId(data.getId());
            shop.setName(data.getName());
            shop.setPhone(data.getPhone());
            shop.setAddress(data.getAddress());
            shop.setEmail(data.getEmail());

            responses.add(shop); // ✅ tambahkan ke list

        }
//        System.out.println("Shop: " + responses.getFirst().getName() + " | ownerId: " + responses.getFirst().getId());
        return responses;
    }

    public void shopValidationWithUserId(Long userId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));

        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("User tidak memiliki akses untuk toko ini");
        }
    }


    public ShopUserNameResponse getShopUserNameResponse(String user, Long shopId){
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));

        ShopUserNameResponse response= new ShopUserNameResponse();

        response.setShopName(shop.getName());
        response.setUsername(user);


        return response;
    }
}
