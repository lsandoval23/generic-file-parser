package org.lsandoval.fileparser.service.parser;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileParserFactory {

    private final Map<String, FileParser> parsersByExtension = new HashMap<>();

    public FileParserFactory(List<FileParser> parsers) {
        for (FileParser parser : parsers) {
            for (String ext : parser.supportedExtensions()) {
                parsersByExtension.put(ext.toLowerCase(), parser);
            }
        }
    }

    public FileParser forExtension(String extension) {
        FileParser parser = parsersByExtension.get(extension.toLowerCase());
        if (parser == null) {
            throw new IllegalArgumentException("No parser registered for file extension: " + extension);
        }
        return parser;
    }
}
