package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.springframework.stereotype.Repository;

@Repository
public interface DisciplineRepository extends BaseRepository<Discipline, Long> {
}
