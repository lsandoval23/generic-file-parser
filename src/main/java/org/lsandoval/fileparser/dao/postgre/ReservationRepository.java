package org.lsandoval.fileparser.dao.postgre;

import org.lsandoval.fileparser.dao.BaseRepository;
import org.lsandoval.fileparser.dao.postgre.model.Reservation;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends BaseRepository<Reservation, Long> {

}
