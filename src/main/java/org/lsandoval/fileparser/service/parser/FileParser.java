package org.lsandoval.fileparser.service.parser;

import org.lsandoval.fileparser.exception.FileProcessingException;
import org.lsandoval.fileparser.service.model.job.ProcessingResult;
import org.lsandoval.fileparser.service.model.parser.ParseRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface FileParser {

    Set<String> supportedExtensions();

    /**
     * Parses {@code file}, mapping each record to a DTO and flushing DTOs to
     * {@code batchProcessor} in batches. Returns a {@link ProcessingResult}
     * describing rows that failed to map (which are skipped, not fatal); the
     * caller folds it into the overall job result alongside persistence results.
     */
    <T> ProcessingResult parse(
            File file,
            ParseRequest request,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor) throws IOException, FileProcessingException;
}
