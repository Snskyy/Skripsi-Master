package com.inventory.inventory.repository;


import com.inventory.inventory.model.SupplierCustomer;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface SupplierCustomerRepository extends JpaRepository<SupplierCustomer, Long> {
    List<SupplierCustomer> findByTypeAndShopIdAndDeleteDateIsNull(String type, Long shopId);

    Optional<SupplierCustomer> findByShopIdAndTypeAndIsDefaultTrue(Long shopId, String type);

}