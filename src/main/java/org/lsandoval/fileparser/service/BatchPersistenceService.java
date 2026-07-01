package org.lsandoval.fileparser.service;

import org.lsandoval.fileparser.cache.model.EntityCache;
import org.lsandoval.fileparser.service.model.PaymentDto;
import org.lsandoval.fileparser.service.model.job.ProcessingResult;
import org.lsandoval.fileparser.service.model.ReservationDto;

import java.util.List;

public interface BatchPersistenceService {

    ProcessingResult persistReservationsBatch(List<ReservationDto> reservationDtoList, EntityCache cache);

    ProcessingResult persistPaymentsBatch(List<PaymentDto> paymentDtoList, EntityCache cache);

}
