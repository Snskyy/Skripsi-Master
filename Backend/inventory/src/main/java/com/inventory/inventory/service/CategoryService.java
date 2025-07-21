package com.inventory.inventory.service;

import com.inventory.inventory.dto.category.CategoryRequest;
import com.inventory.inventory.dto.category.CategoryResponse;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.CategoryNotFoundException;
import com.inventory.inventory.model.Category;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.repository.CategoryRepository;
import com.inventory.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private  CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Category createCategory(CategoryRequest request, Long shopId) {
        if(request.getName()== null|| request.getName().equalsIgnoreCase("")){
            throw new BadRequestException("Nama categori tidak boleh kosong");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setShopId(shopId);
        category.setCreateDate(LocalDateTime.now());
        category.setUpdateDate(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        // Cari kategori
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Kategori tidak ditemukan"));
        if(request.getName()== null|| request.getName().equalsIgnoreCase("")){
            throw new BadRequestException("Nama Kategori tidak boleh kosong");
        }
        if (category.getName().equalsIgnoreCase(request.getName())) {
            throw new BadRequestException("Kategori tidak mengalami perubahan");
        }

        // Update nama
        category.setName(request.getName());
        category.setUpdateDate(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    public List<CategoryResponse> getAllCategoryByShopId(Long shopId) {
        List<Category> categories = categoryRepository.findByShopIdAndDeleteDateIsNull(shopId);
        List<CategoryResponse> result = new ArrayList<>();

        for (Category category : categories) {
            CategoryResponse response = new CategoryResponse();
            response.setId(category.getId());
            response.setName(category.getName());

            int total = productRepository.countActiveByCategoryId(category.getId());
            response.setTotalProduct(total);

            result.add(response);
        }

        return result;
    }

    public List<Product> deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Kategori tidak ditemukan"));
        List<Product> products = productRepository.findByCategoryId(id);
        for (Product product : products) {
            product.setCategoryId(null);
            productRepository.save(product);
        }
        category.setDeleteDate(LocalDateTime.now());
        categoryRepository.save(category);

        return products;
    }


}
