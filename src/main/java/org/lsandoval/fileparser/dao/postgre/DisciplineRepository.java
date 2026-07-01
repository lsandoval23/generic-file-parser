package org.lsandoval.fileparser.dao.postgre;

import org.lsandoval.fileparser.dao.BaseRepository;
import org.lsandoval.fileparser.dao.postgre.model.Discipline;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DisciplineRepository extends BaseRepository<Discipline, Long> {

    List<Discipline> findByNameIn(Set<String> names);
}
