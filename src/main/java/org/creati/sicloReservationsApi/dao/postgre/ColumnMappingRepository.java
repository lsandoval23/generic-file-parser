package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.ColumnMapping;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnMappingRepository extends BaseRepository<ColumnMapping, Long> {
    List<ColumnMapping> findByFileType(String fileType);

    List<ColumnMapping> findByFileTypeAndFileExtension(String fileType, String fileExtension);
}
