package org.creati.sicloReservationsApi.service.model.reports;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationTableDto (
        Long reservationId,
        Long classId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        String orderCreator,
        String paymentMethod,
        String status,
        ClientInfo clientInfo,
        LocationInfo locationInfo,
        String disciplineName,
        String instructorName
){

    public record ClientInfo (
            String name,
            String email,
            String phone
    ){}

    public record LocationInfo (
            String studioName,
            String roomName,
            String country,
            String city
    ){}

    public record ReservationTableSummary(
            String status,
            Long count
    ){}

    @Getter
    public enum ReservationSortField {

        RESERVATION_DATE("reservationDate"),
        RESERVATION_ID("reservationId");

        private final String fieldName;

        ReservationSortField(String fieldName) {
            this.fieldName = fieldName;
        }

        public static ReservationSortField fromValue(String value) {
            for (ReservationSortField field : values()) {
                if (field.fieldName.equalsIgnoreCase(value) || field.name().equalsIgnoreCase(value)) {
                    return field;
                }
            }
            throw new IllegalArgumentException("Invalid sortBy value: " + value);
        }

    }
}
