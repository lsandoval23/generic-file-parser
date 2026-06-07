package org.creati.sicloReservationsApi.dao.postgre.dto;

import org.creati.sicloReservationsApi.service.model.reports.ClientReservationsPaymentsDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ClientReservationsPaymentsProjection {

    Long getClientId();
    String getClientName();
    String getClientEmail();
    String getClientPhone();
    Integer getTotalReservations();
    Integer getTotalPayments();
    BigDecimal getTotalAmountReceived();
    LocalDateTime getLastPaymentDate();
    LocalDate getLastReservationDate();
    String getTopDiscipline();

    default ClientReservationsPaymentsDto toDto() {
        return new ClientReservationsPaymentsDto(
                new ClientReservationsPaymentsDto.ClientInfo(
                        getClientId(),
                        getClientName(),
                        getClientEmail(),
                        getClientPhone()),
                getTotalReservations(),
                getTotalPayments(),
                getTotalAmountReceived(),
                getLastPaymentDate(),
                getLastReservationDate(),
                getTopDiscipline()
        );

    }

}
