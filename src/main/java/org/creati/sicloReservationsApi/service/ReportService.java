package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.reports.ClientReservationsPaymentsDto;
import org.creati.sicloReservationsApi.service.model.reports.PagedResponse;
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationGraphReportDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.reports.SortDirection;
import org.creati.sicloReservationsApi.service.model.reports.TopDisciplineDto;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    // Reservation reports

    ReservationGraphReportDto getReservationGroupedReport(String groupBy, LocalDate from, LocalDate to, String timeUnit);

    PagedResponse<ReservationTableDto> getReservationTable(
            LocalDate from, LocalDate to,  Map<String, String> filters,
            int page, int size, ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir);

    List<TopDisciplineDto> getTopDisciplines(LocalDate from, LocalDate to, int limit);

    List<PaymentTableDto.PaymentMethodSummary> getPaymentMethodSummaries(LocalDate from, LocalDate to);

    // Payments reports

    PagedResponse<PaymentTableDto> getPaymentTable(
            LocalDate from, LocalDate to, Map<String, String> filters,
            int page, int size,
            PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir);

    // Clients reports

    PagedResponse<ClientReservationsPaymentsDto> getClientReservationsPayments(
            LocalDate from, LocalDate to, @Nullable String clientFilter,
            int page, int size,
            String sortField);

    PagedResponse<ReservationTableDto> getReservationTableByClientId(
            LocalDate from, LocalDate to, Long clientId,
            int page, int size,
            ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir);

    PagedResponse<PaymentTableDto> getPaymentTableByClientId(
            LocalDate from, LocalDate to, Long clientId,
            int page, int size,
            PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir);


}

