package org.creati.sicloReservationsApi.service.model.job;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ProcessingResult {

    private boolean success;
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private int skipped;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

}
