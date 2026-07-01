package org.creati.sicloReservationsApi.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Service
public class CsvFileParser extends AbstractFileParser {

    public CsvFileParser(ColumnMappingService columnMappingService, RowMapper rowMapper) {
        super(columnMappingService, rowMapper);
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

        Map<String, String> headerToFieldMap = headerToFieldMapping(request);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, format)) {

            List<String> csvHeaders = csvParser.getHeaderNames();
            validateRequiredHeaders(new HashSet<>(csvHeaders), request);

            BatchAccumulator<T> batch = batchAccumulator(batchSize, batchProcessor);
            for (CSVRecord record : csvParser) {
                Map<String, Object> sourceRecord = new HashMap<>();
                for (String header : csvHeaders) {
                    String value = record.get(header);
                    sourceRecord.put(header, value.isEmpty() ? null : value);
                }
                Map<String, Object> rawRow = toFieldKeyedRow(sourceRecord, headerToFieldMap);
                batch.add(rowMapper.map(rawRow, dtoClass));
            }
            batch.flush();
        }
    }
}
