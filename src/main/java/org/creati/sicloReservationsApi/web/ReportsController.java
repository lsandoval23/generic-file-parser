package org.creati.sicloReservationsApi.web;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.spec.PaymentSpecifications;
import org.creati.sicloReservationsApi.dao.spec.ReservationSpecifications;
import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.model.reports.ClientReservationsPaymentsDto;
import org.creati.sicloReservationsApi.service.model.reports.PagedResponse;
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationGraphReportDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.reports.SortDirection;
import org.creati.sicloReservationsApi.service.model.reports.TopDisciplineDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@PreAuthorize("hasAuthority('REPORT_VIEW')")
@RequestMapping("/reports")
public class ReportsController {

    private final ReportService reportService;

    public ReportsController(final ReportService reportService) {
        this.reportService = reportService;
    }


    /*     Clients Reports
     */

    @GetMapping("/clients")
    public ResponseEntity<PagedResponse<ClientReservationsPaymentsDto>> getReservationsPaymentsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String client,
            @RequestParam(required = false) String sortField,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reportService.getClientReservationsPayments(from, to, client, page, size, sortField));
    }

    @GetMapping("/clients/{clientId}/reservations")
    public ResponseEntity<PagedResponse<ReservationTableDto>> getReservationTableByClient(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PathVariable Long clientId,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                reportService.getReservationTableByClientId(
                        from, to, clientId,
                        page, size,
                        ReservationTableDto.ReservationSortField.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)));

    }

    @GetMapping("/clients/{clientId}/payments")
    public ResponseEntity<PagedResponse<PaymentTableDto>> getPaymentTableByClient(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PathVariable Long clientId,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                reportService.getPaymentTableByClientId(
                        from, to, clientId,
                        page, size,
                        PaymentTableDto.PaymentSortFiled.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)));

    }

    /*     Reservations Reports
     */

    @GetMapping("/reservations/series")
    public ReservationGraphReportDto getReservationGraphReport(
            @RequestParam String groupBy,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String timeUnit
    ) {
        log.info("GroupBy received: {}", groupBy);
        return reportService.getReservationGroupedReport(groupBy, from, to, timeUnit);
    }


    @GetMapping("/reservations/table")
    public ResponseEntity<PagedResponse<ReservationTableDto>> getReservationTable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Map<String, String> allParams
    ) {
        Map<String, String> filters = allParams.entrySet().stream()
                .filter(entry -> ReservationSpecifications.allowedFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ResponseEntity.ok().body(
                reportService.getReservationTable(
                        from, to, filters,
                        page, size,
                        ReservationTableDto.ReservationSortField.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)));
    }

    @GetMapping("/reservations/top-disciplines")
    public ResponseEntity<List<TopDisciplineDto>> getPopularDisciplines(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(reportService.getTopDisciplines(from, to, limit));
    }


    /*     Payments Reports
     */

    @GetMapping("/payments/table")
    public ResponseEntity<PagedResponse<PaymentTableDto>> getPaymentTable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Map<String, String> allParams
    ) {
        Map<String, String> filters = allParams.entrySet().stream()
                .filter(entry -> PaymentSpecifications.allowedFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ResponseEntity.ok().body(
                reportService.getPaymentTable(
                        from, to, filters,
                        page, size,
                        PaymentTableDto.PaymentSortFiled.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)
                )
        );
    }

    @GetMapping("/payments/payment-methods/summary")
    public ResponseEntity<List<PaymentTableDto.PaymentMethodSummary>> getPaymentMethodSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        return ResponseEntity.ok(reportService.getPaymentMethodSummaries(from, to));
    }
}
