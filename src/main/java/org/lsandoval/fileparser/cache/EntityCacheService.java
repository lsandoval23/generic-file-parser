package org.lsandoval.fileparser.cache;

import org.lsandoval.fileparser.cache.model.EntityCache;
import org.lsandoval.fileparser.service.model.PaymentDto;
import org.lsandoval.fileparser.service.model.ReservationDto;

import java.util.List;

public interface EntityCacheService {
    EntityCache preloadEntitiesForReservation(List<ReservationDto> reservations);

    EntityCache preloadEntitiesForPayments(List<PaymentDto> payments);
}
