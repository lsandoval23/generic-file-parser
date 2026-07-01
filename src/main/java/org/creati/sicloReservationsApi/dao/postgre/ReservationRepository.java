package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends BaseRepository<Reservation, Long> {

}
