package org.creati.sicloReservationsApi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.creati.sicloReservationsApi.dao.postgre.ColumnMappingRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.ColumnMapping;
import org.creati.sicloReservationsApi.service.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.mapping.BulkUpdateColumnMappingRequest;
import org.creati.sicloReservationsApi.service.model.mapping.ColumnMappingDto;
import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.mapping.UpdateColumnMappingRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ColumnMappingServiceImpl implements ColumnMappingService {

    private final ColumnMappingRepository columnMappingRepository;

    public ColumnMappingServiceImpl(ColumnMappingRepository columnMappingRepository) {
        this.columnMappingRepository = columnMappingRepository;
    }


    @Override
    public List<ColumnMappingDto> getAllMappings() {
        return columnMappingRepository.findAll().stream()
                .map(ColumnMapping::toDto)
                .toList();
    }

    @Override
    public List<ColumnMappingDto> getMappingsByFileType(String fileType) {
        return columnMappingRepository.findByFileType(fileType).stream()
                .map(ColumnMapping::toDto)
                .toList();
    }

    @Override
    public ColumnMappingDto getMappingById(Long id) {
        return columnMappingRepository.findById(id)
                .map(ColumnMapping::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Column mapping not found with id: " + id));
    }

    @Override
    public ColumnMappingDto updateMapping(Long id, UpdateColumnMappingRequest updateRequest) {
        ColumnMapping existingMapping = columnMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Column mapping not found with id: " + id));

        ColumnMapping mappingModified = existingMapping.toBuilder()
                .sourceField(updateRequest.getExcelHeader())
                .required(updateRequest.getRequired())
                .build();

        ColumnMapping updatedMapping = columnMappingRepository.save(mappingModified);
        return updatedMapping.toDto();
    }

    @Override
    public ProcessingResult bulkUpdateMappings(List<BulkUpdateColumnMappingRequest> updateRequests) {

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (BulkUpdateColumnMappingRequest request : updateRequests) {
            try {
                ColumnMapping existingMapping = columnMappingRepository.findById(request.getMappingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Column mapping not found with id: " + request.getMappingId()));

                ColumnMapping mappingModified = existingMapping.toBuilder()
                        .sourceField(request.getExcelHeader())
                        .required(request.getRequired())
                        .build();

                columnMappingRepository.save(mappingModified);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                String errorMsg = String.format("Failed to update mapping with id %d: %s", request.getMappingId(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        return ProcessingResult.builder()
                .success(failureCount == 0)
                .totalProcessed(updateRequests.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();

    }

    @Override
    public Map<String, String> getHeaderToFieldMapping(String fileType, String fileExtension) {
        List<ColumnMapping> mappings = columnMappingRepository.findByFileTypeAndFileExtension(fileType, fileExtension);
        Map<String, String> headerToFieldMap = new HashMap<>();

        for (ColumnMapping mapping: mappings) {
            String sourceFieldGroup = mapping.getSourceField();
            String fieldName = mapping.getFieldName();

            String[] splitHeaders = sourceFieldGroup.split(";");
            Arrays.stream(splitHeaders)
                    .map(String::trim)
                    .filter(header -> !header.isEmpty())
                    .forEach(header -> headerToFieldMap.put(header, fieldName));

        }

        log.info("Loaded {} active column mappings for file type: {} ({})", headerToFieldMap.size(), fileType, fileExtension);
        return headerToFieldMap;
    }

    @Override
    public Boolean validateRequiredHeaders(Set<String> inputHeaders, String fileType, String fileExtension) {

        // Get required headers
        List<ColumnMapping> requiredMappings = columnMappingRepository.findByFileTypeAndFileExtension(fileType, fileExtension).stream()
                .filter(ColumnMapping::isRequired)
                .toList();

        // Normalized input headers
        Set<String> normalizedInputHeaders = inputHeaders.stream()
                .map(header -> header.toLowerCase().trim())
                .collect(Collectors.toSet());

        List<String> missingHeaders = requiredMappings.stream()
                .map(ColumnMapping::getSourceField)
                .filter(sourceField ->
                        Arrays.stream(sourceField.split(";"))
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .noneMatch(normalizedInputHeaders::contains))
                .toList();

        if (!missingHeaders.isEmpty()) {
            log.warn("Missing required headers for file type {}: {}", fileType, missingHeaders);
            return false;
        }

        return true;
    }

}
