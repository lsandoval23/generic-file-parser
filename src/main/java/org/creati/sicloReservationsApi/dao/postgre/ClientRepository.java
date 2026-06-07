package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ClientReservationsPaymentsProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClientRepository extends BaseRepository<Client, Long> {

    // clientFilter can be null, email or name partial match
    @Query(value = """
            SELECT *
            FROM get_clients_reservations_payments(:fromDate, :toDate, :clientFilter)
            ORDER BY
                CASE WHEN :sortField = 'total_amount_received' THEN total_amount_received END DESC
            LIMIT :limit OFFSET :offset
            """,
            nativeQuery = true)
    List<ClientReservationsPaymentsProjection> getClientReservationsPayments(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("sortField") String sortField,
            @Param("clientFilter") String clientFilter,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM get_clients_reservations_payments(:fromDate, :toDate, :clientFilter)
            """,
            nativeQuery = true)
    long countClientReservationsPayments(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("clientFilter") String clientFilter
    );

    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto(
                    r.reservationId,
                    r.classId,
                    r.reservationDate,
                    r.reservationTime,
                    r.orderCreator,
                    r.paymentMethod,
                    r.status,
                    new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto$ClientInfo(
                        c.name,
                        c.email,
                        c.phone
                    ),
                    new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto$LocationInfo(
                        s.name,
                        rm.name,
                        s.country,
                        s.city
                    ),
                    d.name,
                    i.name
                )
                FROM Reservation r
                JOIN r.client c
                JOIN r.room rm
                JOIN rm.studio s
                JOIN r.discipline d
                JOIN r.instructor i
                WHERE r.reservationDate >= :fromDate AND r.reservationDate <= :toDate
                AND c.clientId = :clientId
            """)
    Page<ReservationTableDto> getReservationTableByClientId(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("clientId") Long clientId,
            Pageable pageable
    );


    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto(
                    p.operationId,
                    p.month,
                    p.day,
                    p.week,
                    p.purchaseDate,
                    p.accreditationDate,
                    p.releaseDate,
                    p.operationType,
                    p.productValue,
                    p.transactionFee,
                    p.amountReceived,
                    p.installments,
                    p.paymentMethod,
                    p.packageName,
                    p.classCount,
                    new org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto$ClientInfo(
                        c.name,
                        c.email,
                        c.phone
                    )
                )
                FROM PaymentTransaction p
                JOIN p.client c
                WHERE p.purchaseDate >= :fromDate AND p.purchaseDate <= :toDate
                AND c.clientId = :clientId
            """)
    Page<PaymentTableDto> getPaymentTableByClientId(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("clientId") Long clientId,
            Pageable pageable
    );

}
