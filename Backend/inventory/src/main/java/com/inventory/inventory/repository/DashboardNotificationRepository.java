package com.inventory.inventory.repository;

import com.inventory.inventory.model.DashboardNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DashboardNotificationRepository extends JpaRepository<DashboardNotification, Long> {

    boolean existsByShopIdAndTypeAndKey(Long shopId, String type, String key);

    List<DashboardNotification> findByShopIdAndResolvedFalseAndExpireDateAfter(Long shopId, LocalDateTime now);

    Optional<DashboardNotification> findByIdAndShopId(Long id, Long shopId);

    List<DashboardNotification> findByShopIdAndTypeAndResolvedFalse(Long shopId, String type);


}

