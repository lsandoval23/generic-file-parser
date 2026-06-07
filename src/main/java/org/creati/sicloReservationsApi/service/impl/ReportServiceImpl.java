package org.creati.sicloReservationsApi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.PaymentTransactionRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ClientReservationsPaymentsProjection;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final ReservationRepository reservationRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final ClientRepository clientRepository;

    public ReportServiceImpl(
            final ReservationRepository reservationRepository,
            final PaymentTransactionRepository paymentRepository,
            final ClientRepository clientRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
    }


    @Override
    public ReservationGraphReportDto getReservationGroupedReport(
            String groupBy,
            LocalDate from,
            LocalDate to,
            String timeUnit) {

        String[] groupByFields = Arrays.stream(groupBy.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        // Validation
        Arrays.stream(groupByFields).forEach(ReservationGraphReportDto.GroupBy::fromValue);

        log.info("Generating reservation grouped report from {} to {} grouped by {} with time unit {}",
                from, to, String.join("; ", groupByFields), timeUnit);
        List<ReservationReportProjection> rows = reservationRepository.getReservationsReportsTimeSeries(groupByFields, timeUnit, from, to);

        LocalDate minPeriodStart = rows.stream()
                .map(ReservationReportProjection::getPeriodStart)
                .min(LocalDate::compareTo)
                .orElse(from);

        LocalDate maxPeriodEnd = rows.stream()
                .map(ReservationReportProjection::getPeriodEnd)
                .max(LocalDate::compareTo)
                .orElse(to);

        Map<String, List<ReservationReportProjection>> grouped = rows.stream()
                .collect(Collectors.groupingBy(ReservationReportProjection::getGroupName));

        List<LocalDate> allPeriods = rows.stream()
                .map(ReservationReportProjection::getPeriodStart)
                .distinct()
                .sorted()
                .toList();

        List<ReservationGraphReportDto.ReservationSeriesDto> series = grouped.entrySet().stream()
                .map(entry -> {
                    Map<LocalDate, Long> totalsByPeriod = entry.getValue().stream()
                            .collect(Collectors.toMap(
                                    ReservationReportProjection::getPeriodStart,
                                    ReservationReportProjection::getTotal
                            ));

                    List<Long> values = allPeriods.stream()
                            .map(period -> totalsByPeriod.getOrDefault(period, 0L))
                            .toList();

                    return new ReservationGraphReportDto.ReservationSeriesDto(entry.getKey(), values);
                })
                .toList();

        return new ReservationGraphReportDto(
                new ReservationGraphReportDto.Range(minPeriodStart, maxPeriodEnd),
                timeUnit,
                series
        );
    }


    @Override
    public PagedResponse<ReservationTableDto> getReservationTable(
            LocalDate from, LocalDate to,
            Map<String, String> filters,
            int page, int size,
            ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.getValue()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Reservation> spec = ReservationSpecifications.dateBetween(from, to);

        if (filters != null && !filters.isEmpty()) {
            for (var entry: filters.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue();

                switch (key) {
                    case "client" -> spec = spec.and(ReservationSpecifications.clientLike(value));
                    case "instructor" -> spec = spec.and(ReservationSpecifications.instructorLike(value));
                    case "discipline" -> spec = spec.and(ReservationSpecifications.disciplineLike(value));
                    default -> log.warn("Unknown RESERVATION filter: {}", key);
                }
            }
        }

        Page<Reservation> pageResponse = reservationRepository.findAll(spec, pageable);
        List<ReservationTableDto> mappedContent = pageResponse.getContent().stream()
                .map(Reservation::toDto)
                .toList();

        // Build summary
        List<ReservationTableDto.ReservationTableSummary> summaryList = reservationRepository.getReservationSummary(from, to);

        long totalAccepted = 0;
        long totalCancelled = 0;
        long totalPending = 0;

        for (ReservationTableDto.ReservationTableSummary summary : summaryList) {
            switch (summary.status().toUpperCase()) {
                case "ACEPTADA" -> totalAccepted = summary.count();
                default -> log.warn("Unknow status: {}", summary.status());
            }
        }

        long totalClasses = totalAccepted + totalCancelled + totalPending;
        Map<String, Object> summary = Map.of(
                "totalClasses", totalClasses,
                "accepted", totalAccepted,
                "cancelled", totalCancelled,
                "pending", totalPending
        );

        return new PagedResponse<>(
                summary,
                mappedContent,
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }

    @Override
    public List<TopDisciplineDto> getTopDisciplines(LocalDate from, LocalDate to, int limit) {
        Pageable topN = PageRequest.of(0, limit);
        LocalDate effectiveFrom = from != null
                ? from
                : LocalDate.of(1970, 1, 1);
        LocalDate effectiveTo = to != null
                ? to
                : LocalDate.now();

        return reservationRepository.findPopularDisciplines(effectiveFrom, effectiveTo, topN);
    }

    @Override
    public List<PaymentTableDto.PaymentMethodSummary> getPaymentMethodSummaries(LocalDate from, LocalDate to) {
        LocalDateTime effectiveFrom = from != null
                ? LocalDateTime.of(from, LocalTime.MIN)
                : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime effectiveTo = to != null
                ? LocalDateTime.of(to, LocalTime.MAX)
                : LocalDateTime.now();

        return paymentRepository.getPaymentSummaryReport(effectiveFrom, effectiveTo);
    }

    @Override
    public PagedResponse<PaymentTableDto> getPaymentTable(
            LocalDate from, LocalDate to,
            Map<String, String> filters,
            int page, int size,
            PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.getValue()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<PaymentTransaction> spec = PaymentSpecifications.purchaseDateBetween(
                LocalDateTime.of(from, LocalTime.MIN),
                LocalDateTime.of(to, LocalTime.MAX));

        if (filters != null && !filters.isEmpty()) {
            for (var entry: filters.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue();

                switch (key) {
                    case "client" -> spec = spec.and(PaymentSpecifications.clientLike(value));
                    case "payment" -> spec = spec.and(PaymentSpecifications.paymentMethodLike(value));
                    default -> log.warn("Unknown PAYMENT filter: {}", key);
                }
            }
        }

        Page<PaymentTransaction> pageResponse = paymentRepository.findAll(spec, pageable);
        List<PaymentTableDto> mappedContent = pageResponse.getContent().stream()
                .map(PaymentTransaction::toDto)
                .toList();

        // Build summary
        List<PaymentTableDto.PaymentTableSummary> summaryList = paymentRepository.getPaymentSummary(
                LocalDateTime.of(from, LocalTime.MIN),
                LocalDateTime.of(to, LocalTime.MAX));

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, Long> operationCounts = new HashMap<>();

        for (PaymentTableDto.PaymentTableSummary summary : summaryList) {
            operationCounts.put(summary.status(), summary.count());
            if (summary.totalAmount() != null) {
                totalAmount = totalAmount.add(summary.totalAmount());
            }
        }

        Map<String, Object> summary = Map.of(
                "totalAmountReceived", totalAmount,
                "operationSummary", operationCounts
        );


        return new PagedResponse<>(
                summary,
                mappedContent,
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }

    @Override
    public PagedResponse<ClientReservationsPaymentsDto> getClientReservationsPayments(
            LocalDate from, LocalDate to,
            @Nullable String clientFilter,
            int page, int size,
            String sortField) {

        int limit = size;
        int offset = page * size;

        List<String> allowedSortFields = List.of("total_amount_received");
        if (Objects.nonNull(sortField) && !allowedSortFields.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }

        List<ClientReservationsPaymentsProjection> rows = clientRepository.getClientReservationsPayments(from, to, sortField, clientFilter, limit, offset);
        long totalElements = clientRepository.countClientReservationsPayments(from, to, clientFilter);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean last = page + 1 >= totalPages;

        List<ClientReservationsPaymentsDto> mappedRows = rows.stream()
                .map(ClientReservationsPaymentsProjection::toDto)
                .toList();

        return new PagedResponse<>(
                null,
                mappedRows,
                page,
                size,
                totalElements,
                totalPages,
                last
        );
    }

    @Override
    public PagedResponse<ReservationTableDto> getReservationTableByClientId(
            LocalDate from, LocalDate to, Long clientId,
            int page, int size,
            ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.getValue()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReservationTableDto> pageResponse = clientRepository.getReservationTableByClientId(from, to, clientId, pageable);

        return new PagedResponse<>(
                null,
                pageResponse.getContent(),
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }

    @Override
    public PagedResponse<PaymentTableDto> getPaymentTableByClientId(
            LocalDate from, LocalDate to, Long clientId,
            int page, int size,
            PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.getValue()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentTableDto> pageResponse = clientRepository.getPaymentTableByClientId(
                LocalDateTime.of(from, LocalTime.MIN),
                LocalDateTime.of(to, LocalTime.MAX),
                clientId,
                pageable);

        return new PagedResponse<>(
                null,
                pageResponse.getContent(),
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }


}
