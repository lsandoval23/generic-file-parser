package org.creati.sicloReservationsApi.service.model.reports;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(
        Map<String, Object> summary,
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
