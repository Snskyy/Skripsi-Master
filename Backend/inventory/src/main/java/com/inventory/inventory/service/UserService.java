package com.inventory.inventory.service;

import com.inventory.inventory.dto.auth.ChangePasswordRequest;
import com.inventory.inventory.dto.auth.ResetPasswordViaOtpRequest;
import com.inventory.inventory.dto.shop.ShopResponse;
import com.inventory.inventory.dto.user.UserResponse;
import com.inventory.inventory.dto.user.UserUpdateRequest;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.User;
import com.inventory.inventory.repository.ShopRepository;
import com.inventory.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private ShopRepository shopRepository;

    public UserResponse getUser(Long userId){
//        Shop shop = shopRepository.findById(shopId)
//                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));
//
//        if (!shop.getUserId().equals(userId)) {
//            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
//        }

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not Found"));
        UserResponse getUserResponse = new UserResponse();
        getUserResponse.setName(user.getName());
        getUserResponse.setGender(user.getGender());
        getUserResponse.setDob(user.getDOB());
        getUserResponse.setCreateDate(user.getCreateDate());
        getUserResponse.setPhone(user.getPhone());
        getUserResponse.setEmail(user.getEmail());
        getUserResponse.setAddress(user.getAddress());
        return getUserResponse;
    }

    public UserResponse updateUser(UserUpdateRequest request, Long userId) {
//        Shop shop = shopRepository.findById(shopId)
//                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));
//
//        if (!shop.getUserId().equals(userId)) {
//            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
//        }

        // ✳️ Validasi manual
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Nama tidak boleh kosong");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Nomor telepon tidak boleh kosong");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new BadRequestException("Alamat tidak boleh kosong");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        // buat validasi jg baik (tapi nanti aja)

        boolean isSame =
                request.getName().trim().equals(user.getName()) &&
                        request.getPhone().trim().equals(user.getPhone()) &&
                        request.getAddress().trim().equals(user.getAddress()) &&
                        (request.getDOB() == null ? user.getDOB() == null : request.getDOB().equals(user.getDOB()));

        if (isSame) {
            throw new BadRequestException("Tidak ada perubahan data untuk disimpan.");
        }
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDOB(request.getDOB());
        user.setUpdateDate(LocalDateTime.now());
        userRepository.save(user); // Save the updated user
       
        return getUser(userId);
    }
    public String changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Email tidak ditemukan"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Password lama salah");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Konfirmasi password tidak cocok");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateDate(LocalDateTime.now());
        userRepository.save(user);
        return "Password berhasil diperbarui";
    }


//    public void resetPasswordByOtp(ResetPasswordViaOtpRequest req) {
//        User user = userRepository.findByEmail(req.getEmail())
//                .orElseThrow(() -> new NotFoundException("Email tidak ditemukan"));
//
//        if (!user.getOtp().equals(req.getOtp())) {
//            throw new BadRequestException("OTP salah.");
//        }
//        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
//            throw new BadRequestException("OTP sudah kadaluarsa.");
//        }
//        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
//            throw new BadRequestException("Konfirmasi password tidak cocok.");
//        }
//
//        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
//        user.setOtp(null);
//        user.setOtpExpiry(null);
//        userRepository.save(user);
//    }

}
