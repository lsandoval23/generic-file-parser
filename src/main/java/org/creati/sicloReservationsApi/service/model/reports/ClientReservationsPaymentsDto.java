package org.creati.sicloReservationsApi.service.model.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientReservationsPaymentsDto(
        ClientInfo clientInfo,
        Integer totalReservations,
        Integer totalPayments,
        BigDecimal totalAmountReceived,
        LocalDateTime lastPaymentDate,
        LocalDate lastReservationDate,
        String topDiscipline

) {

    public record ClientInfo(
            Long clientId,
            String clientName,
            String clientEmail,
            String clientPhone
    ) {

    }

}
