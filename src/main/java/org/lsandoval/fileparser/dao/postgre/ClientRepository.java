package org.lsandoval.fileparser.dao.postgre;

import org.lsandoval.fileparser.dao.BaseRepository;
import org.lsandoval.fileparser.dao.postgre.model.Client;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ClientRepository extends BaseRepository<Client, Long> {

    List<Client> findByEmailIn(Set<String> emails);

}
