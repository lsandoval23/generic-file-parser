package org.creati.sicloReservationsApi.service.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkUpdateColumnMappingRequest {

    @NotNull(message = "El mappingId es requerido")
    @JsonProperty("mappingId")
    private Long mappingId;

    @NotBlank(message = "Excel header no puede estar vacío")
    @JsonProperty("excelHeader")
    private String excelHeader;

    @NotNull(message = "El campo required no puede ser nulo")
    @JsonProperty("required")
    private Boolean required;
}
