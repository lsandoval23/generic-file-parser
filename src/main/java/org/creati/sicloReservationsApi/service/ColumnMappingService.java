package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.mapping.BulkUpdateColumnMappingRequest;
import org.creati.sicloReservationsApi.service.model.mapping.ColumnMappingDto;
import org.creati.sicloReservationsApi.service.model.mapping.UpdateColumnMappingRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ColumnMappingService {

    List<ColumnMappingDto> getAllMappings();

    List<ColumnMappingDto> getMappingsByFileType(String fileType);

    ColumnMappingDto getMappingById(Long id);

    ColumnMappingDto updateMapping(Long id, UpdateColumnMappingRequest updateRequest);

    ProcessingResult bulkUpdateMappings(List<BulkUpdateColumnMappingRequest> updateRequests);

    /**
     * Builds a map from source header (as it appears in the input file) to the
     * target DTO field name, for the given file type and extension.
     */
    Map<String, String> getHeaderToFieldMapping(String fileType, String fileExtension);

    /**
     * Validates that every required header for the given file type/extension is
     * present among the supplied input headers.
     */
    Boolean validateRequiredHeaders(Set<String> inputHeaders, String fileType, String fileExtension);
}
