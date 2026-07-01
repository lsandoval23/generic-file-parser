package org.creati.sicloReservationsApi.service.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Streams a JSON file that contains a top-level array of objects (or an array
 * nested at the path given by the configured {@code file-parser} locator using
 * a simple dot-separated key path, e.g. {@code "data.records"}).
 */
@Slf4j
@Service
public class JsonFileParser extends AbstractFileParser {

    private final ObjectMapper objectMapper;
    private final FileParserProperties fileParserProperties;

    public JsonFileParser(
            ColumnMappingService columnMappingService,
            RowMapper rowMapper,
            ObjectMapper objectMapper,
            FileParserProperties fileParserProperties) {
        super(columnMappingService, rowMapper);
        this.objectMapper = objectMapper;
        this.fileParserProperties = fileParserProperties;
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

        Map<String, String> headerToFieldMap = headerToFieldMapping(request);
        String locator = fileParserProperties.resolve(request.fileType(), request.extension());
        String[] pathSegments = locator != null
                ? locator.split("\\.")
                : new String[0];

        try (JsonParser jp = objectMapper.createParser(file)) {
            navigateToArray(jp, pathSegments);

            // Validate required keys once, against the first object, then stream.
            BatchAccumulator<T> batch = batchAccumulator(batchSize, batchProcessor);
            boolean headersValidated = false;

            while (jp.nextToken() == JsonToken.START_OBJECT) {
                Map<String, Object> jsonRecord = objectMapper.readValue(jp, Map.class);

                if (!headersValidated) {
                    validateRequiredHeaders(new HashSet<>(jsonRecord.keySet()), request);
                    headersValidated = true;
                }

                Map<String, Object> rawRow = toFieldKeyedRow(jsonRecord, headerToFieldMap);
                batch.add(rowMapper.map(rawRow, dtoClass));
            }
            batch.flush();
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
