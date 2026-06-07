package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.ReservationDto;

import java.util.List;

public interface BatchPersistenceService {

    ProcessingResult persistReservationsBatch(List<ReservationDto> reservationDtoList, EntityCache cache);

    ProcessingResult persistPaymentsBatch(List<PaymentDto> paymentDtoList, EntityCache cache);

}
