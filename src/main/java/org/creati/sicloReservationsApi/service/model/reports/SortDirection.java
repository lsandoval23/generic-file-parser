package org.creati.sicloReservationsApi.service.model.reports;

import lombok.Getter;

@Getter
public enum SortDirection {
    ASC("asc"),
    DESC("desc");

    private final String value;

    SortDirection(String value) {
        this.value = value;
    }

    public static SortDirection fromValue(String value) {
        for (SortDirection field : values()) {
            if (field.value.equalsIgnoreCase(value) || field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Invalid sortDir value: " + value);
    }
}
