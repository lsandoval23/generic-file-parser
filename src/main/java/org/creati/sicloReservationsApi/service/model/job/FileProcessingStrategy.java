package org.creati.sicloReservationsApi.service.model.job;

import java.util.List;
import java.util.function.BiConsumer;

public record FileProcessingStrategy<T>(
        Class<T> dtoClass,
        BiConsumer<List<T>, List<ProcessingResult>> persistFunction,
        String extraParam) {

    public void persist(List<?> batch, List<ProcessingResult> batchResults) {
        @SuppressWarnings("unchecked")
        List<T> typedBatch = (List<T>) batch;
        persistFunction.accept(typedBatch, batchResults);
    }
}
