package org.creati.sicloReservationsApi.service.excel.util;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;


@Slf4j
public class ExcelUtils {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_TIME
    );

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    public static Workbook createWorkbook(File file) throws IOException {

        String filename = Optional.of(file.getName())
                .orElseThrow(() -> new IllegalArgumentException("Filename is null"));
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "xlsx" -> new XSSFWorkbook(new FileInputStream(file));
            case "xls"  -> new HSSFWorkbook(new FileInputStream(file));
            default     -> throw new IllegalArgumentException("The provided file is not a valid Excel file: " + extension);
        };
    }

    public static Workbook createStreamingWorkbook(File file) throws IOException {
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!"xlsx".equals(extension)) {
            throw new IllegalArgumentException("Not supported file type only .xlsx files");
        }

        InputStream is = new FileInputStream(file);
        return StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(4096)
                .open(is);
    }


    public static String getBaseName(String filename) {
        if (filename == null) return null;
        int lastSeparator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot < lastSeparator) {
            return filename.substring(lastSeparator + 1);
        }
        return filename.substring(lastSeparator + 1, lastDot);
    }


    public static String getExtension(String filename) {
        if (filename == null) return null;
        int lastSeparator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot < lastSeparator) {
            return ""; // no extension
        }
        return filename.substring(lastDot + 1);
    }

    public static boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }


    public static String getCellStringValue(Cell cell) {
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

    public static Long getCellLongValue(Cell cell) {
        if (cell == null) return null;

        CellType type = cell.getCellType();

        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Long.parseLong(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    log.error("Error parsing Long from cell: {}", cell.getStringCellValue(), e);
                    yield null;
                }
            }
            default -> {
                log.error("Unsupported cell type for Long conversion: {}", cell.getCellType());
                yield null;
            }
        };
    }

    public static Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    log.error("Error parsing Integer from cell: {}", cell.getStringCellValue(), e);
                    yield null;
                }
            }
            default -> {
                log.error("Unsupported cell type for Integer conversion: {}", cell.getCellType());
                yield null;
            }
        };
    }

    public static BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return null;

        CellType type = cell.getCellType();

        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                try {
                    yield new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    log.error("Error parsing BigDecimal from cell: {}", cell.getStringCellValue(), e);
                    yield null;
                }
            }
            default -> {
                log.error("Unsupported cell type for BigDecimal conversion: {}", cell.getCellType());
                yield null;
            }
        };
    }

    public static LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) return null;
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    log.info("failed to parse date {} with formatter {}", value, formatter);
                }
            }
        }

        return null;
    }

    public static LocalTime getCellTimeValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        }

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) return null;
            for (DateTimeFormatter formatter : TIME_FORMATTERS) {
                try {
                    return LocalTime.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    log.info("failed to parse time {} with formatter {}", value, formatter);
                }
            }
        }

        return null;
    }


    public static LocalDateTime getCellDateTimeValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) return null;
            for (DateTimeFormatter dateTimeFormatter : DATE_TIME_FORMATTERS) {
                try {
                    return LocalDateTime.parse(value, dateTimeFormatter);
                } catch (DateTimeParseException ignored) {
                    log.info("failed to parse datetime {} with formatter {}", value, dateTimeFormatter);
                }
            }
            // Try ISO_LOCAL_DATE_TIME as a last resort
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ignored) {
                log.info("failed to parse datetime {} with ISO_LOCAL_DATE_TIME", value);
            }
        }

        return null;
    }



    public static Object convertCellValue(Cell cell, Class<?> targetType) {
        if (cell == null) {
            return null;
        }

        if (targetType == Long.class || targetType == long.class) {
            return ExcelUtils.getCellLongValue(cell);
        } else if (targetType == String.class) {
            return ExcelUtils.getCellStringValue(cell);
        } else if (targetType == LocalDate.class) {
            return ExcelUtils.getCellDateValue(cell);
        } else if (targetType == LocalTime.class) {
            return ExcelUtils.getCellTimeValue(cell);
        } else if (targetType == LocalDateTime.class){
            return ExcelUtils.getCellDateTimeValue(cell);
        } else if (targetType == Integer.class || targetType == int.class) {
            Long value = ExcelUtils.getCellLongValue(cell);
            return value != null ? value.intValue() : null;
        } else if (targetType == Double.class || targetType == double.class) {
            return cell.getNumericCellValue();
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return cell.getBooleanCellValue();
        } else if (targetType == BigDecimal.class ) {
            return ExcelUtils.getCellBigDecimalValue(cell);
        }

        return ExcelUtils.getCellStringValue(cell);
    }








}
