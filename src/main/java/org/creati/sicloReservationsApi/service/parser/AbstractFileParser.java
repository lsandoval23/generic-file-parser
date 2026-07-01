package org.creati.sicloReservationsApi.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.service.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;

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
     */
    protected Map<String, Object> toFieldKeyedRow(Map<String, Object> sourceRecord, Map<String, String> headerToField) {
        Map<String, Object> rawRow = new HashMap<>();
        for (Map.Entry<String, Object> entry : sourceRecord.entrySet()) {
            String fieldName = headerToField.get(entry.getKey());
            if (fieldName != null) {
                rawRow.put(fieldName, entry.getValue());
            } else {
                log.debug("No mapping found for source header '{}'", entry.getKey());
            }
        }
        return rawRow;
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
}
