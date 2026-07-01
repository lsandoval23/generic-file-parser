package org.lsandoval.fileparser.dao.postgre;

import org.lsandoval.fileparser.dao.BaseRepository;
import org.lsandoval.fileparser.dao.postgre.model.PaymentTransaction;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction, Long> {

}
