package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.excel.util.ExcelUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
public class StreamingExcelParser {

    private final ColumnMappingService columnMappingService;

    public StreamingExcelParser(
            final ColumnMappingService columnMappingService) {
        this.columnMappingService = columnMappingService;
    }


    public <T> void parseFromFile(
            File file,
            String fileType,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor,
            @Nullable String sheetName) throws IOException, IllegalArgumentException, FileProcessingException {

        try (Workbook workbook = ExcelUtils.createStreamingWorkbook(file)) {

            Map<String, String> headerToFieldMap = columnMappingService.getHeaderToFieldMapping(fileType);
            Sheet sheet = Optional.ofNullable(sheetName)
                    .map(workbook::getSheet)
                    .orElse(workbook.getSheetAt(0));
            Iterator<Row> rowIterator = sheet.iterator();

            // Validate if the sheet is empty
            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("The Excel Header is empty.");
            }

            Row headerRow = rowIterator.next();
            Map<Integer, String> columnIndexToFieldMap = new HashMap<>();

            // Validate required headers
            Set<String> excelHeaders = new HashSet<>();
            headerRow.forEach(cell -> excelHeaders.add(ExcelUtils.getCellStringValue(cell)));
            if (!columnMappingService.validateRequiredHeaders(excelHeaders, fileType)) {
                throw new IllegalArgumentException("Missing required headers in the Excel file.");
            }

            //Build mapping of column indexes to field names
            for (Cell cell : headerRow) {
                String headerValue = ExcelUtils.getCellStringValue(cell);
                String fieldName = headerToFieldMap.get(headerValue);

                if (fieldName != null) {
                    columnIndexToFieldMap.put(cell.getColumnIndex(), fieldName);
                    log.debug("Mapped column {} '{}' to field '{}'", cell.getColumnIndex(), headerValue, fieldName);
                } else {
                    log.warn("No mapping found for header '{}' at column {}", headerValue, cell.getColumnIndex());
                }
            }

            // Process data rows
            List<T> batch = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row == null || ExcelUtils.isEmptyRow(row)) {
                    continue;
                }

                // Parse row into DTO
                T dto = parseRowDynamic(row, columnIndexToFieldMap, dtoClass);
                batch.add(dto);

                // Process batch if size reached
                if (batch.size() >= batchSize) {
                    batchProcessor.accept(batch);
                    batch.clear();
                }
            }

            // Process any remaining records
            if (!batch.isEmpty()) {
                batchProcessor.accept(batch);
                batch.clear();
            }
        }

    }

    private <T> T parseRowDynamic(Row row, Map<Integer, String> columnIndexToFieldMap, Class<T> dtoClass) throws RuntimeException {

        try {
            // Create a new instance of the DTO class
            T dto = dtoClass.getDeclaredConstructor().newInstance();

            // Set fields based on column mappings
            for (Map.Entry<Integer, String> entry : columnIndexToFieldMap.entrySet()) {
                int columnIndex = entry.getKey();
                String fieldName = entry.getValue();
                Cell cell = row.getCell(columnIndex);
                setFieldValue(dto, fieldName, cell);
            }
            return dto;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Reflection error creating instance of {}: {}", dtoClass.getSimpleName(), e.getMessage());
            throw new FileProcessingException("Error creating instance of " + dtoClass.getSimpleName(), e);
        } catch (RuntimeException ex) {
            log.error("Error setting field value: {}", ex.getMessage());
            throw new FileProcessingException("Error setting field value", ex);
        }

    }


    private <T> void setFieldValue(T dto, String fieldName, Cell cell) throws RuntimeException{

        try {
            // Build setter method name (e.g., setFieldName) and find the method
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method setter = Arrays.stream(dto.getClass().getMethods())
                    .filter(m -> m.getName().equals(setterName) && m.getParameterCount() == 1)
                    .findFirst()
                    .orElse(null);

            if (setter == null) {
                log.warn("No setter found for field: {}", fieldName);
                return;
            }

            // Convert cell value to the appropriate type and invoke the setter
            Class<?> paramType = setter.getParameterTypes()[0];
            Object value = ExcelUtils.convertCellValue(cell, paramType);
            setter.invoke(dto, value);

        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Reflection error invoking setter for field '{}': {}", fieldName, e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
