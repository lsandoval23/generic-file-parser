package org.lsandoval.fileparser.dao.postgre;

import org.lsandoval.fileparser.dao.BaseRepository;
import org.lsandoval.fileparser.dao.postgre.model.FileJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface FileJobRepository extends BaseRepository<FileJob, Long> {

    Page<FileJob> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

}
