package org.lsandoval.fileparser.service.parser;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds the {@code file-parser.locators} configuration: a map of
 * {@code fileType -> (fileExtension -> locator)}. The locator tells each parser
 * where its records live (Excel sheet name, JSON array property, or XML
 * repeating element). An empty/blank locator means "use the format default".
 */
@Data
@Component
@ConfigurationProperties(prefix = "file-parser")
public class FileParserProperties {

    private Map<String, Map<String, String>> locators = new HashMap<>();

    /**
     * Resolves the record locator for the given file type and extension.
     * Lookups are case-insensitive (file type upper-cased, extension lower-cased).
     *
     * @return the configured locator, or {@code null} when none is configured or
     *         the configured value is blank (i.e. format default).
     */
    public String resolve(String fileType, String fileExtension) {
        if (fileType == null || fileExtension == null) {
            return null;
        }
        Map<String, String> byExtension = locators.get(fileType.toUpperCase());
        if (byExtension == null) {
            return null;
        }
        String locator = byExtension.get(fileExtension.toLowerCase());
        return (locator == null || locator.isBlank()) ? null : locator;
    }
}
