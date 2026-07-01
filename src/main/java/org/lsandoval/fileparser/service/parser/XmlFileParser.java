package org.lsandoval.fileparser.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.lsandoval.fileparser.exception.FileProcessingException;
import org.lsandoval.fileparser.service.ColumnMappingService;
import org.lsandoval.fileparser.service.model.parser.ParseRequest;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Streams an XML file and treats repeating child elements as records.
 * The configured {@code file-parser} locator specifies the repeating element
 * name (e.g. {@code "record"} or {@code "row"}). If null, the direct children
 * of the root element are used as records.
 */
@Slf4j
@Service
public class XmlFileParser extends AbstractFileParser {

    private final FileParserProperties fileParserProperties;

    public XmlFileParser(ColumnMappingService columnMappingService, RowMapper rowMapper,
                         FileParserProperties fileParserProperties) {
        super(columnMappingService, rowMapper);
        this.fileParserProperties = fileParserProperties;
    }

    @Override
    public Set<String> supportedExtensions() {
        return Set.of("xml");
    }

    @Override
    public <T> void parse(
            File file,
            ParseRequest request,
            Class<T> dtoClass,
            int batchSize,
            Consumer<List<T>> batchProcessor) throws IOException, FileProcessingException {

        Map<String, String> headerToFieldMap = headerToFieldMapping(request);
        String recordElement = fileParserProperties.resolve(request.fileType(), request.extension());

        XMLInputFactory factory = XMLInputFactory.newInstance();
        // Disable external entity processing to prevent XXE
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try (InputStream is = new FileInputStream(file)) {
            XMLStreamReader reader = factory.createXMLStreamReader(is);

            BatchAccumulator<T> batch = batchAccumulator(batchSize, batchProcessor);
            boolean headersValidated = false;
            boolean inRecord = false;
            String currentElement = null;
            Map<String, Object> currentRecord = null;
            int depth = 0;
            int recordDepth = -1;

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        String localName = reader.getLocalName();
                        depth++;
                        if (!inRecord) {
                            // Start of a record element
                            if (recordElement == null || recordElement.equals(localName)) {
                                inRecord = true;
                                recordDepth = depth;
                                currentRecord = new HashMap<>();
                            }
                        } else if (depth == recordDepth + 1) {
                            currentElement = localName;
                        }
                    }
                    case XMLStreamConstants.CHARACTERS -> {
                        if (inRecord && currentElement != null && currentRecord != null) {
                            String text = reader.getText().trim();
                            if (!text.isEmpty()) {
                                currentRecord.merge(currentElement, text, (a, b) -> a.toString() + b.toString());
                            }
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        if (inRecord && depth == recordDepth + 1) {
                            currentElement = null;
                        }
                        if (inRecord && depth == recordDepth) {
                            // End of record element — map it
                            if (currentRecord != null && !currentRecord.isEmpty()) {
                                if (!headersValidated) {
                                    validateRequiredHeaders(new HashSet<>(currentRecord.keySet()), request);
                                    headersValidated = true;
                                }

                                Map<String, Object> rawRow = toFieldKeyedRow(currentRecord, headerToFieldMap);
                                batch.add(rowMapper.map(rawRow, dtoClass));
                            }
                            inRecord = false;
                            currentRecord = null;
                            recordDepth = -1;
                        }
                        depth--;
                    }
                    default -> { /* skip */ }
                }
            }
            reader.close();

            batch.flush();

        } catch (XMLStreamException e) {
            throw new FileProcessingException("Error parsing XML file: " + e.getMessage(), e);
        }
    }
}
