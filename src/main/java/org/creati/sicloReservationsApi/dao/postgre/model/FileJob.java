package org.creati.sicloReservationsApi.dao.postgre.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.service.model.job.FileJobDto;
import org.creati.sicloReservationsApi.service.model.job.FileType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Entity
@Table(name = "file_processing_job")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private String fileName;

    private String fileExtension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant finishedAt;

    private Integer totalRecords;
    private Integer processedRecords;
    private Integer skippedRecords;
    private Integer errorRecords;

    @Column(columnDefinition = "TEXT")
    private String processingResult;


    public enum JobStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESS,
        FAILED
    }

    public FileJobDto toDto(ObjectMapper objectMapper) {

        ZoneId lima = ZoneId.of("America/Lima");
        return new FileJobDto(
                this.getJobId(),
                this.getFileName(),
                this.getFileType().name(),
                this.getStatus().name(),
                this.getErrorMessage(),
                Optional.ofNullable(this.getCreatedAt())
                        .map(instant -> LocalDateTime.ofInstant(instant, lima))
                        .orElse(null),
                Optional.ofNullable(this.getFinishedAt())
                        .map(instant -> LocalDateTime.ofInstant(instant, lima))
                        .orElse(null),
                Optional.ofNullable(this.getProcessingResult())
                        .map(string -> {
                            try {
                                return objectMapper.readTree(string);
                            } catch (JsonProcessingException exception) {
                                return null;
                            }
                        })
                        .orElse(null)
        );
    }



}
