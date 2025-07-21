package com.inventory.inventory.repository;

import com.inventory.inventory.dto.transaction.FinanceSummaryResponse;
import com.inventory.inventory.model.TransactionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionHeaderRepository extends JpaRepository<TransactionHeader, Long> {
//    @Query("SELECT COUNT(t) FROM TransactionHeader t WHERE t.type = :type AND DATE(t.createDate) = :date")
//    long countByTypeAndDate(@Param("type") String type, @Param("date") LocalDate date);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<TransactionHeader> findByInvoiceNumber(String invoiceNumber);
    List<TransactionHeader> findByShopIdAndStatus(Long shopId, String status);

    List<TransactionHeader> findByShopIdOrderByCreateDateDesc(Long shopId);
    List<TransactionHeader> findByShopIdAndStatusOrderByCreateDateDesc(Long shopId, String status);

    @Query(value = """
    SELECT 
        TO_CHAR(th.create_date, 'YYYY-MM-DD') AS date,
        SUM(CASE WHEN th.type = 'SALES' THEN th.total ELSE 0 END) AS revenue,
        SUM(CASE WHEN th.type = 'PURCHASE' THEN th.total ELSE 0 END) AS expenditure
    FROM transaction_headers th
    WHERE th.status = 'FINAL' AND th.delete_date IS NULL
      AND th.shop_id = :shopId
      AND th.create_date >= :startDate AND th.create_date < :endDate
    GROUP BY TO_CHAR(th.create_date, 'YYYY-MM-DD')
    ORDER BY TO_CHAR(th.create_date, 'YYYY-MM-DD')
""", nativeQuery = true)
    List<FinanceSummaryResponse> findSummaryGroupedByDay(
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end,
            @Param("shopId") Long shopId
    );


    @Query(value = """
    SELECT 
        CONCAT(EXTRACT(YEAR FROM th.create_date), '-W', LPAD(EXTRACT(WEEK FROM th.create_date)::TEXT, 2, '0')) AS date,
        SUM(CASE WHEN th.type = 'SALES' THEN th.total ELSE 0 END) AS revenue,
        SUM(CASE WHEN th.type = 'PURCHASE' THEN th.total ELSE 0 END) AS expenditure
    FROM transaction_headers th
    WHERE th.status = 'FINAL' AND th.delete_date IS NULL
      AND th.shop_id = :shopId
      AND th.create_date >= :startDate AND th.create_date < :endDate
    GROUP BY EXTRACT(YEAR FROM th.create_date), EXTRACT(WEEK FROM th.create_date)
    ORDER BY EXTRACT(YEAR FROM th.create_date), EXTRACT(WEEK FROM th.create_date)
""", nativeQuery = true)
    List<FinanceSummaryResponse> findSummaryGroupedByWeek(
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end,
            @Param("shopId") Long shopId
    );


    @Query(value = """
    SELECT 
        TO_CHAR(th.create_date, 'YYYY-MM') AS date,
        SUM(CASE WHEN th.type = 'SALES' THEN th.total ELSE 0 END) AS revenue,
        SUM(CASE WHEN th.type = 'PURCHASE' THEN th.total ELSE 0 END) AS expenditure
    FROM transaction_headers th
    WHERE th.status = 'FINAL' AND th.delete_date IS NULL
      AND th.shop_id = :shopId
      AND th.create_date >= :startDate AND th.create_date < :endDate
    GROUP BY TO_CHAR(th.create_date, 'YYYY-MM')
    ORDER BY TO_CHAR(th.create_date, 'YYYY-MM')
""", nativeQuery = true)
    List<FinanceSummaryResponse> findSummaryGroupedByMonth(
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end,
            @Param("shopId") Long shopId
    );


    @Query(value = """
    SELECT 
        EXTRACT(YEAR FROM th.create_date)::TEXT AS date,
        SUM(CASE WHEN th.type = 'SALES' THEN th.total ELSE 0 END) AS revenue,
        SUM(CASE WHEN th.type = 'PURCHASE' THEN th.total ELSE 0 END) AS expenditure
    FROM transaction_headers th
    WHERE th.status = 'FINAL' AND th.delete_date IS NULL
      AND th.shop_id = :shopId
      AND th.create_date >= :startDate AND th.create_date < :endDate
    GROUP BY EXTRACT(YEAR FROM th.create_date)
    ORDER BY EXTRACT(YEAR FROM th.create_date)
""", nativeQuery = true)
    List<FinanceSummaryResponse> findSummaryGroupedByYear(
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end,
            @Param("shopId") Long shopId
    );


    @Query("SELECT COUNT(t) FROM TransactionHeader t WHERE t.shopId = :shopId AND UPPER(t.status) = UPPER(:status)")
    int countByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") String status);

    @Query("""
    SELECT COUNT(th)
    FROM TransactionHeader th
    WHERE th.shopId = :shopId
      AND th.type = 'PURCHASE'
      AND UPPER(th.status) = 'RECEIVED'
""")
    int countReceivedPurchaseTransactions(@Param("shopId") Long shopId);

    @Query("SELECT SUM(th.total) FROM TransactionHeader th WHERE th.shopId = :shopId AND th.type = :type AND th.status = :status AND th.deleteDate IS NULL")
    Long sumTotalByTypeAndStatus(@Param("shopId") Long shopId,
                                 @Param("type") String type,
                                 @Param("status") String status);
    @Query("SELECT th.id, th.invoiceNumber, th.createDate " +
            "FROM TransactionHeader th " +
            "WHERE th.shopId = :shopId AND th.type = 'SALES' AND th.status = 'FINAL' " +
            "ORDER BY th.createDate DESC")
    List<Object[]> findLastSalesTransaction(@Param("shopId") Long shopId);


    @Query("SELECT FUNCTION('DATE', th.createDate), SUM(th.total) " +
            "FROM TransactionHeader th " +
            "WHERE th.shopId = :shopId AND th.type = 'SALES' AND th.status = 'FINAL' " +
            "AND th.createDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', th.createDate) ORDER BY FUNCTION('DATE', th.createDate)")
    List<Object[]> sumRevenueByDate(@Param("shopId") Long shopId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE', th.createDate), SUM(th.total) " +
            "FROM TransactionHeader th " +
            "WHERE th.shopId = :shopId AND th.type = 'PURCHASE' AND th.status = 'FINAL' " +
            "AND th.createDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', th.createDate) ORDER BY FUNCTION('DATE', th.createDate)")
    List<Object[]> sumExpenseByDate(@Param("shopId") Long shopId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM TransactionHeader t WHERE t.type = :type AND t.shopId = :shopId AND DATE(t.createDate) = :date")
    long countByTypeAndShopIdAndDate(@Param("type") String type, @Param("shopId") Long shopId, @Param("date") LocalDate date);

}
