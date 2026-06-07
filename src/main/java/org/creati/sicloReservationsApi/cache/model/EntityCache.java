package org.creati.sicloReservationsApi.cache.model;

import lombok.Data;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.creati.sicloReservationsApi.dao.postgre.model.Instructor;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.creati.sicloReservationsApi.dao.postgre.model.Studio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class EntityCache {

    private Map<String, Client> clientsByEmail = new HashMap<>();
    private Map<String, Studio> studiosByName = new HashMap<>();
    private Map<String, Room> roomsByStudioAndName = new HashMap<>();
    private Map<String, Discipline> disciplinesByName = new HashMap<>();
    private Map<String, Instructor> instructorsByName = new HashMap<>();
    private Set<Long> existingReservationIds = new HashSet<>();
    private Set<Long> existingOperationIds = new HashSet<>();

}
