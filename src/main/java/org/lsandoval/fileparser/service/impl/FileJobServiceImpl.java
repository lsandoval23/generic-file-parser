package org.lsandoval.fileparser.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lsandoval.fileparser.dao.postgre.FileJobRepository;
import org.lsandoval.fileparser.dao.postgre.model.FileJob;
import org.lsandoval.fileparser.service.FileJobService;
import org.lsandoval.fileparser.service.model.job.FileJobCreateRequest;
import org.lsandoval.fileparser.service.model.job.FileJobDto;
import org.lsandoval.fileparser.service.model.job.FileJobUpdateRequest;
import org.lsandoval.fileparser.service.model.common.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class FileJobServiceImpl implements FileJobService {

    private final FileJobRepository fileJobRepository;
    private final ObjectMapper objectMapper;

    public FileJobServiceImpl(
            final FileJobRepository fileJobRepository,
            final ObjectMapper objectMapper) {
        this.fileJobRepository = fileJobRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public FileJob createFileJob(FileJobCreateRequest createRequest) {
        return fileJobRepository.save(FileJob.builder()
                .fileName(createRequest.getFileName())
                .fileExtension(createRequest.getFileExtension())
                .fileType(createRequest.getFileType())
                .status(FileJob.JobStatus.PENDING)
                .updatedAt(Instant.now())
                .createdAt(Instant.now())
                .build());
    }

    @Override
    public void updateStatus(Long jobId, FileJobUpdateRequest updateRequest, FileJob existingJob) {
        fileJobRepository.save(FileJob.builder()
                .jobId(jobId)
                .fileName(existingJob.getFileName())
                .fileExtension(existingJob.getFileExtension())
                .fileType(existingJob.getFileType())
                .status(updateRequest.getStatus())
                .errorMessage(updateRequest.getErrorMessage())
                .createdAt(existingJob.getCreatedAt())
                .updatedAt(Instant.now())
                .finishedAt(updateRequest.getFinishedAt())
                .totalRecords(updateRequest.getTotalRecords())
                .processedRecords(updateRequest.getProcessedRecords())
                .skippedRecords(updateRequest.getSkippedRecords())
                .errorRecords(updateRequest.getErrorRecords())
                .processingResult(updateRequest.getProcessingResult())
                .build());


    }

    @Override
    public Optional<FileJob> getFileJobById(Long jobId) {
        return fileJobRepository.findById(jobId);
    }

    @Override
    public PagedResponse<FileJobDto> getFileJobs(
            LocalDate from, LocalDate to,
            int page, int size) {

        ZoneId lima = ZoneId.of("America/Lima");
        Pageable pageable = PageRequest.of(page, size);

        Instant start =  from.atStartOfDay(lima).toInstant();
        Instant end = to.atTime(LocalTime.MAX).atZone(lima).toInstant();

        Page<FileJob> pageResponse = fileJobRepository.findByCreatedAtBetween(start, end, pageable);
        List<FileJobDto> mappedContent = pageResponse.getContent().stream()
                .map(item -> item.toDto(objectMapper))
                .toList();

        return new PagedResponse<>(
                null,
                mappedContent,
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }
}
