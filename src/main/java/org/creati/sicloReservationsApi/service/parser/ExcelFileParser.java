package org.creati.sicloReservationsApi.service.parser;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.parser.ParseRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Service
public class ExcelFileParser extends AbstractFileParser {

    private final FileParserProperties fileParserProperties;

    public ExcelFileParser(ColumnMappingService columnMappingService, RowMapper rowMapper,
                           FileParserProperties fileParserProperties) {
        super(columnMappingService, rowMapper);
        this.fileParserProperties = fileParserProperties;
    }

    @Override
    public Set<String> supportedExtensions() {
        return Set.of("xlsx");
    }

    @Override
    public <T> void parse(
            File file,
            ParseRequest request,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor) throws IOException, FileProcessingException {

        try (Workbook workbook = createStreamingWorkbook(file)) {
            Map<String, String> headerToFieldMap = headerToFieldMapping(request);

            String sheetName = fileParserProperties.resolve(request.fileType(), request.extension());
            Sheet sheet = Optional.ofNullable(sheetName)
                    .map(workbook::getSheet)
                    .orElse(workbook.getSheetAt(0));

            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("The Excel sheet is empty.");
            }

            Row headerRow = rowIterator.next();
            Set<String> excelHeaders = new HashSet<>();
            headerRow.forEach(cell -> excelHeaders.add(getCellStringValue(cell)));

            validateRequiredHeaders(excelHeaders, request);

            Map<Integer, String> columnIndexToField = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerValue = getCellStringValue(cell);
                String fieldName = headerToFieldMap.get(headerValue);
                if (fieldName != null) {
                    columnIndexToField.put(cell.getColumnIndex(), fieldName);
                    log.debug("Mapped column {} '{}' to field '{}'", cell.getColumnIndex(), headerValue, fieldName);
                } else {
                    log.warn("No mapping found for Excel header '{}' at column {}", headerValue, cell.getColumnIndex());
                }
            }

            BatchAccumulator<T> batch = batchAccumulator(batchSize, batchProcessor);
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row == null || isEmptyRow(row)) continue;

                Map<String, Object> rawRow = extractRow(row, columnIndexToField);
                batch.add(rowMapper.map(rawRow, dtoClass));
            }
            batch.flush();
        }
    }

    private Map<String, Object> extractRow(Row row, Map<Integer, String> columnIndexToField) {
        Map<String, Object> rawRow = new HashMap<>();
        for (Map.Entry<Integer, String> entry : columnIndexToField.entrySet()) {
            Cell cell = row.getCell(entry.getKey());
            rawRow.put(entry.getValue(), extractCellValue(cell));
        }
        return rawRow;
    }

    private Object extractCellValue(Cell cell) {
        if (cell == null) return null;

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                }
                yield cell.getNumericCellValue();
            }
            case STRING -> cell.getStringCellValue().trim();
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> null;
        };
    }

    private Workbook createStreamingWorkbook(File file) throws IOException {
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!"xlsx".equals(extension)) {
            throw new IllegalArgumentException("Unsupported file type for streaming workbook: " + extension);
        }

        InputStream is = new FileInputStream(file);
        try {
            return StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(4096)
                    .open(is);
        } catch (Exception e) {
            is.close();
            throw e;
        }
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

}
