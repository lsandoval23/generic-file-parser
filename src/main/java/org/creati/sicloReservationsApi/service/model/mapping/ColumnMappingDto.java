package org.creati.sicloReservationsApi.service.model.mapping;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ColumnMappingDto {

    private Long mappingId;
    private String fileType;
    private String fieldName;
    private String excelHeader;
    private Boolean required;
    private String dataType;
    private LocalDateTime createdAt;

}
