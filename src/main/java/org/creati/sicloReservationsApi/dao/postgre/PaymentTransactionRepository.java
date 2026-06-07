package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction, Long> {

    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto$PaymentTableSummary(
                    p.status,
                    COUNT(p),
                    SUM(p.amountReceived)
                )
                FROM PaymentTransaction p
                WHERE p.purchaseDate >= :fromDate AND p.purchaseDate <= :toDate
                GROUP BY p.status
            """)
    List<PaymentTableDto.PaymentTableSummary> getPaymentSummary(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query("SELECT DISTINCT p.paymentMethod FROM PaymentTransaction p WHERE p.paymentMethod IS NOT NULL ORDER BY p.paymentMethod")
    List<String> findDistinctPaymentMethods();

    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto$PaymentMethodSummary(
                    p.paymentMethod,
                    COALESCE(SUM(p.amountReceived), 0),
                    COUNT(p)
                )
                FROM PaymentTransaction p
                WHERE p.purchaseDate BETWEEN :fromDate AND :toDate
                GROUP BY p.paymentMethod
                ORDER BY SUM(p.amountReceived) DESC
            """)
    List<PaymentTableDto.PaymentMethodSummary> getPaymentSummaryReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );


}
