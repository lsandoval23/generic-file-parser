package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends BaseRepository<Room, Long> {
}
