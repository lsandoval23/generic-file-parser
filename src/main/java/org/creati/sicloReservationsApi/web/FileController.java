package org.creati.sicloReservationsApi.web;


import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.service.FileJobService;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.creati.sicloReservationsApi.service.excel.util.ExcelUtils;
import org.creati.sicloReservationsApi.service.model.job.FileJobCreateRequest;
import org.creati.sicloReservationsApi.service.model.job.FileJobDto;
import org.creati.sicloReservationsApi.service.model.job.FileType;
import org.creati.sicloReservationsApi.service.model.reports.PagedResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@PreAuthorize("hasAuthority('FILE_UPLOAD')")
@RequestMapping("/files")
public class FileController {

    private final FileProcessingService fileProcessingService;
    private final FileJobService fileJobService;

    public FileController(
            final FileProcessingService fileProcessingService,
            final FileJobService fileJobService) {
        this.fileProcessingService = fileProcessingService;
        this.fileJobService = fileJobService;
    }

    @PostMapping(value = "/upload/reservations",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processReservation(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        try {
            // Saving in local storage temporarily for processing asynchronously
            String filename = Optional.ofNullable(fileContent.getOriginalFilename())
                    .orElseThrow(() -> new IllegalArgumentException("Filename is null"));

            Path tempFilePath = Files.createTempFile(
                    String.format("%s-%s", ExcelUtils.getBaseName(filename), UUID.randomUUID()),
                    "." + ExcelUtils.getExtension(filename).toLowerCase());
            fileContent.transferTo(tempFilePath);
            File tempFile = tempFilePath.toFile();

            // Start job tracking in DB
            FileJob createdJob = fileJobService.createFileJob(FileJobCreateRequest.builder()
                    .fileName(tempFile.getName())
                    .fileExtension(ExcelUtils.getExtension(filename).toLowerCase())
                    .fileType(FileType.RESERVATION)
                    .build());

            // Process the file asynchronously
            fileProcessingService.processFile(tempFile, createdJob.getJobId(), FileType.RESERVATION);

            return ResponseEntity.accepted().body("File request accepted for processing with Job ID: " + createdJob.getJobId());
        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + fileContent.getOriginalFilename());
        }
    }

    @PostMapping(value = "/upload/payments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processPayment(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        try {
            // Saving in local storage temporarily for processing asynchronously
            String filename = Optional.ofNullable(fileContent.getOriginalFilename())
                    .orElseThrow(() -> new IllegalArgumentException("Filename is null"));

            Path tempFilePath = Files.createTempFile(
                    String.format("%s-%s", ExcelUtils.getBaseName(filename), UUID.randomUUID()),
                    "." + ExcelUtils.getExtension(filename).toLowerCase());
            fileContent.transferTo(tempFilePath);
            File tempFile = tempFilePath.toFile();

            // Start job tracking in DB
            FileJob createdJob = fileJobService.createFileJob(FileJobCreateRequest.builder()
                    .fileName(tempFile.getName())
                    .fileExtension(ExcelUtils.getExtension(filename).toLowerCase())
                    .fileType(FileType.PAYMENT)
                    .build());

            // Process the file asynchronously
            fileProcessingService.processFile(tempFile, createdJob.getJobId(), FileType.PAYMENT);

            return ResponseEntity.accepted().body("File request accepted for processing with Job ID: " + createdJob.getJobId());
        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + fileContent.getOriginalFilename());
        }
    }


    @GetMapping("/jobs")
    public ResponseEntity<PagedResponse<FileJobDto>> getFileJobsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(fileJobService.getFileJobs(from, to, page, size));
    }


}
