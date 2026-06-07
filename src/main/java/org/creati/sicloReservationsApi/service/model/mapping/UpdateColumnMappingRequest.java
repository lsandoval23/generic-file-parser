package org.creati.sicloReservationsApi.service.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateColumnMappingRequest {

    @NotBlank(message = "Excel header no puede estar vacío")
    @JsonProperty("excelHeader")
    private String excelHeader;

    @NotNull(message = "El campo required no puede ser nulo")
    @JsonProperty("required")
    private Boolean required;

}
