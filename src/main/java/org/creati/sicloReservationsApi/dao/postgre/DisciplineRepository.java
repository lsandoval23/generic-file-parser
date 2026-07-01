package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DisciplineRepository extends BaseRepository<Discipline, Long> {

    List<Discipline> findByNameIn(Set<String> names);
}
