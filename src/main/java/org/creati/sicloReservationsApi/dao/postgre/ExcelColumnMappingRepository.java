package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.ExcelColumnMapping;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExcelColumnMappingRepository extends BaseRepository<ExcelColumnMapping, Long> {
    List<ExcelColumnMapping> findByFileType(String fileType);
}
