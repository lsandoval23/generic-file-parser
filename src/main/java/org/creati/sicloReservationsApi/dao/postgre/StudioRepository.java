package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Studio;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioRepository extends BaseRepository<Studio, Long> {
}
