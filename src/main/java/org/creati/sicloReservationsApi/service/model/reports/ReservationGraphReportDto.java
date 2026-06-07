package org.creati.sicloReservationsApi.service.model.reports;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public record ReservationGraphReportDto(
        Range range,
        String timeUnit,
        List<ReservationSeriesDto> series
) {

    public record Range(
            LocalDate from,
            LocalDate to
    ) {
    }

    public record ReservationSeriesDto(
            String group,
            List<Long> values
    ) {
    }

    @Getter
    public enum GroupBy {
        STUDIO("studio"),
        DISCIPLINE("discipline"),
        INSTRUCTOR("instructor");

        private final String fieldName;

        GroupBy(String fieldName) {
            this.fieldName = fieldName;
        }

        public static GroupBy fromValue(String value) {
            for (GroupBy field : values()) {
                if (field.fieldName.equalsIgnoreCase(value) || field.name().equalsIgnoreCase(value)) {
                    return field;
                }
            }
            throw new IllegalArgumentException("Invalid groupBy value: " + value);
        }
    }
}
