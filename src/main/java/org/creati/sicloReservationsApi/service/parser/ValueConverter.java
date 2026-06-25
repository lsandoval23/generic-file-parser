package org.creati.sicloReservationsApi.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
public class ValueConverter {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_TIME
    );

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    /**
     * Converts {@code raw} to {@code targetType}. {@code raw} may be a String,
     * Number, Boolean, LocalDate, LocalTime, LocalDateTime, or null.
     */
    public Object convert(Object raw, Class<?> targetType) {
        if (raw == null) return null;

        // Already the right type
        if (targetType.isInstance(raw)) return raw;

        String asString = raw.toString().trim();

        if (targetType == String.class) return asString;

        if (targetType == Long.class || targetType == long.class) return toLong(raw, asString);
        if (targetType == Integer.class || targetType == int.class) {
            Long l = toLong(raw, asString);
            return l != null ? l.intValue() : null;
        }
        if (targetType == Double.class || targetType == double.class) return toDouble(raw, asString);
        if (targetType == BigDecimal.class) return toBigDecimal(raw, asString);
        if (targetType == Boolean.class || targetType == boolean.class) return toBoolean(raw, asString);
        if (targetType == LocalDate.class) return toLocalDate(raw, asString);
        if (targetType == LocalTime.class) return toLocalTime(raw, asString);
        if (targetType == LocalDateTime.class) return toLocalDateTime(raw, asString);

        // Fallback — return the string representation
        return asString;
    }

    private Long toLong(Object raw, String asString) {
        if (raw instanceof Number n) return n.longValue();
        if (asString.isEmpty()) return null;
        try {
            return Long.parseLong(asString);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse Long from '{}'", asString);
            return null;
        }
    }

    private Double toDouble(Object raw, String asString) {
        if (raw instanceof Number n) return n.doubleValue();
        if (asString.isEmpty()) return null;
        try {
            return Double.parseDouble(asString);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse Double from '{}'", asString);
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object raw, String asString) {
        if (raw instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (asString.isEmpty()) return null;
        try {
            return new BigDecimal(asString);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse BigDecimal from '{}'", asString);
            return null;
        }
    }

    private Boolean toBoolean(Object raw, String asString) {
        if (raw instanceof Boolean b) return b;
        return Boolean.parseBoolean(asString);
    }

    private LocalDate toLocalDate(Object raw, String asString) {
        if (raw instanceof LocalDate ld) return ld;
        if (raw instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (asString.isEmpty()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(asString, fmt);
            } catch (DateTimeParseException ignored) {
                log.debug("Cannot parse LocalDate '{}' with {}", asString, fmt);
            }
        }
        log.warn("Cannot parse LocalDate from '{}'", asString);
        return null;
    }

    private LocalTime toLocalTime(Object raw, String asString) {
        if (raw instanceof LocalTime lt) return lt;
        if (raw instanceof LocalDateTime ldt) return ldt.toLocalTime();
        if (asString.isEmpty()) return null;
        for (DateTimeFormatter fmt : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(asString, fmt);
            } catch (DateTimeParseException ignored) {
                log.debug("Cannot parse LocalTime '{}' with {}", asString, fmt);
            }
        }
        log.warn("Cannot parse LocalTime from '{}'", asString);
        return null;
    }

    private LocalDateTime toLocalDateTime(Object raw, String asString) {
        if (raw instanceof LocalDateTime ldt) return ldt;
        if (asString.isEmpty()) return null;
        for (DateTimeFormatter fmt : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(asString, fmt);
            } catch (DateTimeParseException ignored) {
                log.debug("Cannot parse LocalDateTime '{}' with {}", asString, fmt);
            }
        }
        log.warn("Cannot parse LocalDateTime from '{}'", asString);
        return null;
    }
}
