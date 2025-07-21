package com.inventory.inventory.repository;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.model.Stock;
import com.inventory.inventory.model.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockHistoryRepository  extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findByTransactionDetailId(Long transactionDetailId);
    List<StockHistory> findByStockId(Long stockId);
}
