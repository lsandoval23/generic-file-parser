package org.creati.sicloReservationsApi.service.model.reports;

public record TopDisciplineDto(
        String disciplineName,
        Long totalReservations
) {
}
