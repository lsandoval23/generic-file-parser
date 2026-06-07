package org.creati.sicloReservationsApi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private String code;
    private String message;
    private String path;
    private Instant timestamp;

}
