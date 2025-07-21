package com.inventory.inventory.service;

import com.inventory.inventory.dto.suppliercustomer.SupplierCustomerRequest;
import com.inventory.inventory.dto.suppliercustomer.SupplierCustomerResponse;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.model.SupplierCustomer;
import com.inventory.inventory.repository.SupplierCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierCustomerService {

    @Autowired
    private  SupplierCustomerRepository supplierCustomerRepository;

    // ✅ Ambil list berdasarkan tipe
    public List<SupplierCustomerResponse> getByTypeAndShop(String type, Long shopId) {
        return supplierCustomerRepository.findByTypeAndShopIdAndDeleteDateIsNull(type.toUpperCase(), shopId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Tambah data baru
    public SupplierCustomerResponse save(Long shopId, SupplierCustomerRequest req) {
        if (req.getName()== null || req.getName().equalsIgnoreCase("")){
            throw new BadRequestException("Nama tidak boleh kosong");
        }
        SupplierCustomer entity = fromRequest(req);
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
//        entity.setDeleteDate(null);
        entity.setShopId(shopId);
        SupplierCustomer saved = supplierCustomerRepository.save(entity);
        return toResponse(saved);
    }

    // ✅ Update data berdasarkan ID
    public SupplierCustomerResponse update(Long id, SupplierCustomerRequest req) {
        SupplierCustomer existing = supplierCustomerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Data tidak ditemukan"));
        if (req.getName()== null || req.getName().equalsIgnoreCase("")){
            throw new BadRequestException("Nama tidak boleh kosong");
        }
        // Validasi: tidak ada perubahan
        boolean noChange =
                Objects.equals(existing.getName(), req.getName()) &&
                        Objects.equals(existing.getPhone(), req.getPhone()) &&
                        Objects.equals(existing.getEmail(), req.getEmail()) &&
                        Objects.equals(existing.getNote(), req.getNote());

        if (noChange) {
            throw new BadRequestException("Tidak ada perubahan data");
        }

        existing.setName(req.getName());
        existing.setPhone(req.getPhone());
        existing.setEmail(req.getEmail());
        existing.setNote(req.getNote());
        existing.setUpdateDate(LocalDateTime.now());

        SupplierCustomer updated = supplierCustomerRepository.save(existing);
        return toResponse(updated);
    }

    // ✅ Soft delete (set deleteDate)
    public void softDelete(Long id) {
        SupplierCustomer existing = supplierCustomerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));
        if (existing.getIsDefault()) {
            throw new BadRequestException("Data default tidak boleh dihapus");
        }
        existing.setDeleteDate(LocalDateTime.now());
        supplierCustomerRepository.save(existing);
    }

    // 🔄 Convert entity → response DTO
    public SupplierCustomerResponse toResponse(SupplierCustomer entity) {
        SupplierCustomerResponse dto = new SupplierCustomerResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setNote(entity.getNote());
        dto.setIsDefault(entity.getIsDefault() != null ? entity.getIsDefault() : false);
        dto.setCreateDate(entity.getCreateDate());
        dto.setUpdateDate(entity.getUpdateDate());
        dto.setLastTransactionDate(entity.getLastTransactionDate());
        return dto;
    }

    // 🔄 Convert request DTO → entity
    public SupplierCustomer fromRequest(SupplierCustomerRequest req) {
        SupplierCustomer entity = new SupplierCustomer();
        entity.setName(req.getName());
        entity.setType(req.getType());
        entity.setPhone(req.getPhone());
        entity.setEmail(req.getEmail());
        entity.setNote(req.getNote());
        return entity;
    }
}
