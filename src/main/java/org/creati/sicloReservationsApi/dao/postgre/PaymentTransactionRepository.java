package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction, Long> {

}
