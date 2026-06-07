package org.creati.sicloReservationsApi.service.model.job;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record FileJobDto(
        Long jobId,
        String fileName,
        String fileType,
        String status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime finishedAt,
        JsonNode processingResult
) {
}
