package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RoomRepository extends BaseRepository<Room, Long> {

    @Query("SELECT r FROM Room r JOIN FETCH r.studio s WHERE s.name IN :studioNames")
    List<Room> findByStudioNamesWithStudio(@Param("studioNames") Set<String> studioNames);
}
