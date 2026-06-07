package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Instructor;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorRepository extends BaseRepository<Instructor, Long> {
}
