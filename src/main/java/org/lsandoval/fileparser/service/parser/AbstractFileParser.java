package org.lsandoval.fileparser.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.lsandoval.fileparser.service.ColumnMappingService;
import org.lsandoval.fileparser.service.model.job.ProcessingResult;
import org.lsandoval.fileparser.service.model.parser.ParseRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Shared scaffolding for the concrete {@link FileParser} implementations.
 * <p>
 * Every format parser follows the same skeleton: resolve the header→field
 * mapping, validate required headers, translate each source record into a
 * DTO-field-keyed row, map it onto a DTO and flush DTOs in batches. Only the
 * format-specific reading of headers and records differs, so that logic stays
 * in the subclasses while the common scaffolding lives here.
 */
@Slf4j
public abstract class AbstractFileParser implements FileParser {

    protected final ColumnMappingService columnMappingService;
    protected final RowMapper rowMapper;

    protected AbstractFileParser(ColumnMappingService columnMappingService, RowMapper rowMapper) {
        this.columnMappingService = columnMappingService;
        this.rowMapper = rowMapper;
    }

    /** Source header (as it appears in the file) → target DTO field name. */
    protected Map<String, String> headerToFieldMapping(ParseRequest request) {
        return columnMappingService.getHeaderToFieldMapping(request.fileType(), request.extension());
    }

    /** Throws {@link IllegalArgumentException} if a required header is missing. */
    protected void validateRequiredHeaders(Set<String> sourceHeaders, ParseRequest request) {
        if (!columnMappingService.validateRequiredHeaders(sourceHeaders, request.fileType(), request.extension())) {
            throw new IllegalArgumentException(
                    "Missing required headers in the " + request.extension() + " file.");
        }
    }

    /**
     * Translates a source-keyed record (source header → raw value) into a
     * DTO-field-keyed row, dropping any header that has no configured mapping.
     * <p>
     * Header lookup is case-insensitive to stay consistent with the
     * case-insensitive required-header validation in {@code ColumnMappingService};
     * otherwise a file could pass validation yet silently lose a column whose
     * casing differs from the stored {@code source_field}.
     */
    protected Map<String, Object> toFieldKeyedRow(Map<String, Object> sourceRecord, Map<String, String> headerToField) {
        Map<String, Object> rawRow = new HashMap<>();
        for (Map.Entry<String, Object> entry : sourceRecord.entrySet()) {
            String fieldName = headerToField.get(normalizeHeader(entry.getKey()));
            if (fieldName != null) {
                rawRow.put(fieldName, entry.getValue());
            } else {
                log.debug("No mapping found for source header '{}'", entry.getKey());
            }
        }
        return rawRow;
    }

    /**
     * Normalizes a source header for lookup against the header→field map.
     * Must mirror how {@code ColumnMappingService.getHeaderToFieldMapping} keys
     * that map (lower-cased, trimmed).
     */
    protected static String normalizeHeader(String header) {
        return header == null ? null : header.toLowerCase().trim();
    }

    /**
     * Maps a field-keyed row onto a DTO and adds it to the batch. A row that
     * fails to map is recorded in {@code errors} and skipped instead of aborting
     * the whole file — mirroring the per-row error handling on the persistence
     * side. Batch flush (persistence) errors are left to propagate.
     */
    protected <T> void mapAndAccumulate(
            Map<String, Object> rawRow, Class<T> dtoClass, BatchAccumulator<T> batch, MappingErrors errors) {
        int row = errors.nextRow();
        T dto;
        try {
            dto = rowMapper.map(rawRow, dtoClass);
        } catch (RuntimeException e) {
            errors.record(row, e);
            log.warn("Skipping unmappable row {}: {}", row, e.getMessage());
            return;
        }
        batch.add(dto);
    }

    protected <T> BatchAccumulator<T> batchAccumulator(int batchSize, Consumer<List<T>> batchProcessor) {
        return new BatchAccumulator<>(batchSize, batchProcessor);
    }

    /**
     * Collects mapped DTOs and hands them to the processor whenever the batch
     * size is reached. Call {@link #flush()} once at the end to emit the tail.
     */
    protected static final class BatchAccumulator<T> {
        private final int batchSize;
        private final Consumer<List<T>> batchProcessor;
        private final List<T> batch = new java.util.ArrayList<>();

        private BatchAccumulator(int batchSize, Consumer<List<T>> batchProcessor) {
            this.batchSize = batchSize;
            this.batchProcessor = batchProcessor;
        }

        public void add(T item) {
            batch.add(item);
            if (batch.size() >= batchSize) {
                flush();
            }
        }

        public void flush() {
            if (!batch.isEmpty()) {
                batchProcessor.accept(batch);
                batch.clear();
            }
        }
    }

    /**
     * Accumulates per-row mapping failures during a parse so they can be folded
     * into the job's {@link ProcessingResult} exactly like persistence failures,
     * instead of aborting the whole file. The retained message list is capped to
     * avoid unbounded memory/DB growth on pathological input.
     */
    protected static final class MappingErrors {
        private static final int MAX_RETAINED_MESSAGES = 100;

        private int rowNumber = 0;
        private int failureCount = 0;
        private final List<String> messages = new ArrayList<>();

        /** Advances to the next row and returns its 1-based number. */
        public int nextRow() {
            return ++rowNumber;
        }

        public void record(int row, Exception e) {
            failureCount++;
            if (messages.size() < MAX_RETAINED_MESSAGES) {
                messages.add(String.format("Error mapping row %d: %s", row, e.getMessage()));
            }
        }

        /**
         * A {@link ProcessingResult} representing only the mapping-phase failures.
         * Successfully mapped rows are counted later by the persistence phase, so
         * they are deliberately excluded here to avoid double counting when the
         * batch results are aggregated.
         */
        public ProcessingResult toProcessingResult() {
            return ProcessingResult.builder()
                    .success(failureCount == 0)
                    .totalProcessed(failureCount)
                    .successCount(0)
                    .failureCount(failureCount)
                    .skipped(0)
                    .errors(messages)
                    .build();
        }
    }
}
