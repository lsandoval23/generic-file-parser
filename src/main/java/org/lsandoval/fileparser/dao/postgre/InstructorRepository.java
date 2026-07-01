package org.lsandoval.fileparser.dao.postgre;

import org.lsandoval.fileparser.dao.BaseRepository;
import org.lsandoval.fileparser.dao.postgre.model.Instructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface InstructorRepository extends BaseRepository<Instructor, Long> {

    List<Instructor> findByNameIn(Set<String> names);
}
