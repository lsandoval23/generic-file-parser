package org.creati.sicloReservationsApi.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.impl.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Service
public class CsvFileParser implements FileParser {

    private final ColumnMappingService columnMappingService;
    private final RowMapper rowMapper;

    public CsvFileParser(ColumnMappingService columnMappingService, RowMapper rowMapper) {
        this.columnMappingService = columnMappingService;
        this.rowMapper = rowMapper;
    }

    @Override
    public Set<String> supportedExtensions() {
        return Set.of("csv");
    }

    @Override
    public <T> void parse(
            File file,
            ParseRequest request,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor) throws IOException, FileProcessingException {

        Map<String, String> headerToFieldMap = columnMappingService.getHeaderToFieldMapping(request.fileType());

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, format)) {

            Set<String> csvHeaders = new HashSet<>(csvParser.getHeaderNames());
            if (!columnMappingService.validateRequiredHeaders(csvHeaders, request.fileType())) {
                throw new IllegalArgumentException("Missing required headers in the CSV file.");
            }

            // Build mapping from CSV header name → DTO field name
            Map<String, String> csvHeaderToField = new HashMap<>();
            for (String csvHeader : csvParser.getHeaderNames()) {
                String fieldName = headerToFieldMap.get(csvHeader);
                if (fieldName != null) {
                    csvHeaderToField.put(csvHeader, fieldName);
                    log.debug("Mapped CSV header '{}' to field '{}'", csvHeader, fieldName);
                } else {
                    log.warn("No mapping found for CSV header '{}'", csvHeader);
                }
            }

            List<T> batch = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                Map<String, Object> rawRow = new HashMap<>();
                for (Map.Entry<String, String> entry : csvHeaderToField.entrySet()) {
                    String value = record.get(entry.getKey());
                    rawRow.put(entry.getValue(), value.isEmpty() ? null : value);
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
}
