package org.creati.sicloReservationsApi.dao.postgre.dto;

import java.time.LocalDate;

public interface ReservationReportProjection {
    String getGroupName();
    LocalDate getPeriodStart();
    LocalDate getPeriodEnd();
    Long getTotal();
}
