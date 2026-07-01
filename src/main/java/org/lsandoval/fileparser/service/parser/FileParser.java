package org.lsandoval.fileparser.service.parser;

import org.lsandoval.fileparser.exception.FileProcessingException;
import org.lsandoval.fileparser.service.model.parser.ParseRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface FileParser {

    Set<String> supportedExtensions();

    <T> void parse(
            File file,
            ParseRequest request,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor) throws IOException, FileProcessingException;
}
