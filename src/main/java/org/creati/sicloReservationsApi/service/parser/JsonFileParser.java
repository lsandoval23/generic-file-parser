package org.creati.sicloReservationsApi.service.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.impl.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Streams a JSON file that contains a top-level array of objects (or an array
 * nested at the path given by {@link ParseRequest#sourceHint()} using a
 * simple dot-separated key path, e.g. {@code "data.records"}).
 */
@Slf4j
@Service
public class JsonFileParser implements FileParser {

    private final ColumnMappingService columnMappingService;
    private final RowMapper rowMapper;
    private final ObjectMapper objectMapper;

    public JsonFileParser(
            ColumnMappingService columnMappingService,
            RowMapper rowMapper,
            ObjectMapper objectMapper) {
        this.columnMappingService = columnMappingService;
        this.rowMapper = rowMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Set<String> supportedExtensions() {
        return Set.of("json");
    }

    @Override
    public <T> void parse(
            File file,
            ParseRequest request,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor) throws IOException, FileProcessingException {

        Map<String, String> headerToFieldMap = columnMappingService.getHeaderToFieldMapping(request.fileType());
        String[] pathSegments = request.sourceHint() != null
                ? request.sourceHint().split("\\.")
                : new String[0];

        try (JsonParser jp = objectMapper.createParser(file)) {
            navigateToArray(jp, pathSegments);

            // Collect headers from first object to validate required fields
            // We do a two-pass approach only for validation: validate once, then stream
            List<T> batch = new ArrayList<>();
            boolean headersValidated = false;

            while (jp.nextToken() == JsonToken.START_OBJECT) {
                Map<String, Object> jsonRecord = objectMapper.readValue(jp, Map.class);

                if (!headersValidated) {
                    Set<String> jsonKeys = new HashSet<>(jsonRecord.keySet());
                    if (!columnMappingService.validateRequiredHeaders(jsonKeys, request.fileType())) {
                        throw new IllegalArgumentException("Missing required keys in the JSON file.");
                    }
                    headersValidated = true;
                }

                Map<String, Object> rawRow = new HashMap<>();
                for (Map.Entry<String, Object> entry : jsonRecord.entrySet()) {
                    String fieldName = headerToFieldMap.get(entry.getKey());
                    if (fieldName != null) {
                        rawRow.put(fieldName, entry.getValue());
                    } else {
                        log.debug("No mapping found for JSON key '{}'", entry.getKey());
                    }
                }

                batch.add(rowMapper.map(rawRow, dtoClass));
                if (batch.size() >= batchSize) {
                    batchProcessor.accept(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                batchProcessor.accept(batch);
            }
        }
    }

    private void navigateToArray(JsonParser jp, String[] pathSegments) throws IOException {
        for (String segment : pathSegments) {
            JsonToken token;
            while ((token = jp.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME && segment.equals(jp.getCurrentName())) {
                    jp.nextToken(); // move to START_ARRAY or START_OBJECT
                    break;
                }
            }
        }
        // Advance to START_ARRAY if not already there
        JsonToken current = jp.currentToken();
        if (current != JsonToken.START_ARRAY) {
            while (jp.nextToken() != null) {
                if (jp.currentToken() == JsonToken.START_ARRAY) break;
            }
        }
    }
}
