package com.inventory.inventory.service;

import com.inventory.inventory.dto.auth.AuthResponse;
import com.inventory.inventory.dto.auth.LoginRequest;
import com.inventory.inventory.dto.auth.RegisterRequest;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.SupplierCustomer;
import com.inventory.inventory.model.User;
import com.inventory.inventory.repository.ShopRepository;
import com.inventory.inventory.repository.SupplierCustomerRepository;
import com.inventory.inventory.repository.UserRepository;
import com.inventory.inventory.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SupplierCustomerRepository supplierCustomerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public void registerUser(RegisterRequest request) {

        if (request.getShopName() == null || request.getShopName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama toko tidak boleh kosong");
        }
        if (request.getShopEmail() == null || request.getShopEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email toko tidak boleh kosong");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email pengguna tidak boleh kosong");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password harus minimal 6 karakter");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pengguna tidak boleh kosong");
        }

        if (shopRepository.findByName(request.getShopName()).isPresent()) {
            throw new IllegalArgumentException("Nama toko sudah digunakan, gunakan nama lain");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        Shop shop = new Shop();
        shop.setName(request.getShopName());
        shop.setEmail(request.getShopEmail());
        shop.setAddress(request.getShopAddress());
        shop.setPhone(request.getShopPhone());
        shop.setCreateDate(LocalDateTime.now());
        shop.setUpdateDate(LocalDateTime.now());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setDOB(request.getDOB());
        user.setCreateDate(LocalDateTime.now());
        user.setUpdateDate(LocalDateTime.now());
        user.setRole("OWNER");
        User saveUser = userRepository.save(user);
        shop.setUserId(saveUser.getId());
        Shop savedShop = shopRepository.save(shop);

        SupplierCustomer defaultCustomer = new SupplierCustomer();
        defaultCustomer.setShopId(savedShop.getId());
        defaultCustomer.setName("Umum");
        defaultCustomer.setType("CUSTOMER");
        defaultCustomer.setNote("Default Customer");
        defaultCustomer.setIsDefault(true);
        defaultCustomer.setCreateDate(LocalDateTime.now());
        supplierCustomerRepository.save(defaultCustomer);

        // Generate default supplier
        SupplierCustomer defaultSupplier = new SupplierCustomer();
        defaultSupplier.setShopId(savedShop.getId());
        defaultSupplier.setName("Umum");
        defaultSupplier.setType("SUPPLIER");
        defaultSupplier.setNote("Default Supplier");
        defaultSupplier.setIsDefault(true);
        defaultSupplier.setCreateDate(LocalDateTime.now());
        supplierCustomerRepository.save(defaultSupplier);

    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email atau kata sandi salah, silakan coba lagi."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email atau kata sandi salah, silakan coba lagi.");
        }

        String jwt = jwtUtil.generateToken(user);
        return new AuthResponse("Bearer " + jwt); // Prefixed for frontend use
    }
}
