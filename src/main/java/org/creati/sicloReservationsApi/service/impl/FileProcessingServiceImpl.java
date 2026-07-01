package org.creati.sicloReservationsApi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.BatchPersistenceService;
import org.creati.sicloReservationsApi.service.FileJobService;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.creati.sicloReservationsApi.service.model.job.FileJobUpdateRequest;
import org.creati.sicloReservationsApi.service.model.job.FileProcessingStrategy;
import org.creati.sicloReservationsApi.service.model.job.FileType;
import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.creati.sicloReservationsApi.service.parser.FileParser;
import org.creati.sicloReservationsApi.service.parser.FileParserFactory;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;
import org.creati.sicloReservationsApi.service.util.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    public static final int MAX_ITEMS_IN_BATCH = 1000;

    private final FileParserFactory fileParserFactory;
    private final EntityCacheService entityCacheService;
    private final BatchPersistenceService batchPersistenceService;
    private final FileJobService fileJobService;
    private final ObjectMapper objectMapper;

    public FileProcessingServiceImpl(
            final FileParserFactory fileParserFactory,
            final EntityCacheService entityCacheService,
            final BatchPersistenceService batchPersistenceService,
            final FileJobService fileJobService,
            final ObjectMapper objectMapper) {
        this.fileParserFactory = fileParserFactory;
        this.entityCacheService = entityCacheService;
        this.batchPersistenceService = batchPersistenceService;
        this.fileJobService = fileJobService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Async
    public void processFile(File fileData, Long jobId, FileType fileType) {
        log.info("Starting processing file: {} of type {} with jobId: {}", fileData.getName(), fileType, jobId);

        FileJob jobFound = fileJobService.getFileJobById(jobId)
                .orElseThrow(() -> new FileProcessingException("File job not found with id: " + jobId));
        fileJobService.updateStatus(jobFound.getJobId(), FileJobUpdateRequest.builder()
                .status(FileJob.JobStatus.IN_PROGRESS)
                .build(), jobFound);

        try {
            List<ProcessingResult> batchResults = new ArrayList<>();
            FileProcessingStrategy<?> strategy = getFileProcessingStrategy(fileType);

            String extension = FileUtils.getExtension(fileData.getName()).toLowerCase();
            FileParser parser = fileParserFactory.forExtension(extension);
            ParseRequest parseRequest = new ParseRequest(fileType.name(), extension);

            parser.parse(fileData, parseRequest, strategy.dtoClass(), MAX_ITEMS_IN_BATCH,
                    (batch) -> strategy.persist(batch, batchResults));

            ProcessingResult batchProcessingResult = ProcessingResult.builder()
                    .success(batchResults.stream().allMatch(ProcessingResult::isSuccess))
                    .totalProcessed(batchResults.stream().mapToInt(ProcessingResult::getTotalProcessed).sum())
                    .successCount(batchResults.stream().mapToInt(ProcessingResult::getSuccessCount).sum())
                    .skipped(batchResults.stream().mapToInt(ProcessingResult::getSkipped).sum())
                    .failureCount(batchResults.stream().mapToInt(ProcessingResult::getFailureCount).sum())
                    .errors(batchResults.stream().flatMap(r -> r.getErrors().stream()).toList())
                    .build();

            FileJob.JobStatus jobStatus = batchProcessingResult.isSuccess()
                    ? FileJob.JobStatus.SUCCESS
                    : FileJob.JobStatus.FAILED;

            FileJobUpdateRequest updateRequest = FileJobUpdateRequest.builder()
                    .status(jobStatus)
                    .finishedAt(Instant.now())
                    .totalRecords(batchProcessingResult.getTotalProcessed())
                    .processedRecords(batchProcessingResult.getSuccessCount())
                    .skippedRecords(batchProcessingResult.getSkipped())
                    .errorRecords(batchProcessingResult.getFailureCount())
                    .processingResult(objectMapper.writeValueAsString(batchProcessingResult))
                    .build();

            fileJobService.updateStatus(jobId, updateRequest, jobFound);
            log.info("Completed processing file: {} (type: {}) with jobId: {}. Result: {}",
                    fileData.getName(), fileType, jobId, batchProcessingResult);

        } catch (FileProcessingException | IOException exception) {
            String errorMsg = String.format("Error processing file %s of type %s: %s", fileData.getName(), fileType, exception.getMessage());
            log.error(errorMsg, exception);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .status(FileJob.JobStatus.FAILED)
                    .errorMessage(errorMsg)
                    .build(), jobFound);
        } catch (IllegalArgumentException illegalArgumentException) {
            String errorMsg = String.format("Error in input file format: %s", illegalArgumentException.getMessage());
            log.error(errorMsg, illegalArgumentException);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .errorMessage(errorMsg)
                    .status(FileJob.JobStatus.FAILED)
                    .build(), jobFound);
        } catch (RuntimeException e) {
            String errorMsg = String.format("Unexpected error processing file %s of type %s: %s", fileData.getName(), fileType, e.getMessage());
            log.error(errorMsg, e);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .status(FileJob.JobStatus.FAILED)
                    .errorMessage(errorMsg)
                    .build(), jobFound);
        } finally {
            // Clean up the temporary file
            if (fileData.exists()) {
                boolean deleted = fileData.delete();
                if (!deleted) {
                    log.warn("Failed to delete temporary file: {}", fileData.getAbsolutePath());
                }
            }
        }
    }

    private FileProcessingStrategy<?> getFileProcessingStrategy(FileType fileType) {

        Map<FileType, FileProcessingStrategy<?>> strategyMap = Map.of(
                FileType.RESERVATION, new FileProcessingStrategy<>(
                        ReservationDto.class,
                        (batch, batchResults) -> {
                            EntityCache cache = entityCacheService.preloadEntitiesForReservation(batch);
                            ProcessingResult batchResult = batchPersistenceService.persistReservationsBatch(batch, cache);
                            batchResults.add(batchResult);
                            log.info("Processed batch of {} reservations. Result: {}", batch.size(), batchResult);
                        }
                ),
                FileType.PAYMENT, new FileProcessingStrategy<>(
                        PaymentDto.class,
                        (batch, batchResults) -> {
                            EntityCache cache = entityCacheService.preloadEntitiesForPayments(batch);
                            ProcessingResult batchResult =  batchPersistenceService.persistPaymentsBatch(batch, cache);
                            batchResults.add(batchResult);
                            log.info("Processed batch of {} payments. Result: {}", batch.size(), batchResult);
                        }
                )
        );

        FileProcessingStrategy<?> strategy = strategyMap.get(fileType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
        return strategy;
    }


}
