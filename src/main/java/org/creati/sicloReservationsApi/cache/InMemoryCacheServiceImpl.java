package org.creati.sicloReservationsApi.cache;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.DisciplineRepository;
import org.creati.sicloReservationsApi.dao.postgre.InstructorRepository;
import org.creati.sicloReservationsApi.dao.postgre.PaymentTransactionRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.RoomRepository;
import org.creati.sicloReservationsApi.dao.postgre.StudioRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InMemoryCacheServiceImpl implements EntityCacheService {

    private final ClientRepository clientRepository;
    private final StudioRepository studioRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentTransactionRepository paymentRepository;

    public InMemoryCacheServiceImpl(
            final ClientRepository clientRepository,
            final StudioRepository studioRepository,
            final DisciplineRepository disciplineRepository,
            final InstructorRepository instructorRepository,
            final RoomRepository roomRepository,
            final ReservationRepository reservationRepository,
            final PaymentTransactionRepository paymentRepository) {
        this.clientRepository = clientRepository;
        this.studioRepository = studioRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public EntityCache preloadEntitiesForReservation(List<ReservationDto> reservations) {
        EntityCache entityCache = new EntityCache();

        Set<String> clientEmails = reservations.stream()
                .map(ReservationDto::getClientEmail)
                .collect(Collectors.toSet());
        clientRepository.findByEmailIn(clientEmails)
                .forEach(c -> entityCache.getClientsByEmail().put(c.getEmail(), c));

        Set<String> studioNames = reservations.stream()
                .map(ReservationDto::getStudioName)
                .collect(Collectors.toSet());
        studioRepository.findByNameIn(studioNames)
                .forEach(s -> entityCache.getStudiosByName().put(s.getName(), s));

        Set<String> disciplineNames = reservations.stream()
                .map(ReservationDto::getDisciplineName)
                .collect(Collectors.toSet());
        disciplineRepository.findByNameIn(disciplineNames)
                .forEach(d -> entityCache.getDisciplinesByName().put(d.getName(), d));

        Set<String> instructorNames = reservations.stream()
                .map(ReservationDto::getInstructorName)
                .collect(Collectors.toSet());
        instructorRepository.findByNameIn(instructorNames)
                .forEach(i -> entityCache.getInstructorsByName().put(i.getName(), i));

        // Rooms — fetch only those belonging to the referenced studios, with studio eagerly loaded
        roomRepository.findByStudioNamesWithStudio(studioNames).forEach(room -> {
            if (room.getStudio() == null) {
                log.warn("Room {} has no associated studio; skipping cache entry", room.getRoomId());
                return;
            }
            String key = room.getStudio().getName() + "|" + room.getName();
            entityCache.getRoomsByStudioAndName().put(key, room);
        });

        Set<Long> reservationIds = reservations.stream()
                .map(ReservationDto::getReservationId)
                .collect(Collectors.toSet());
        reservationRepository.findAllById(reservationIds).stream()
                .map(Reservation::getReservationId)
                .forEach(entityCache.getExistingReservationIds()::add);

        return entityCache;
    }

    @Override
    public EntityCache preloadEntitiesForPayments(List<PaymentDto> payments) {
        EntityCache entityCache = new EntityCache();

        Set<Long> operationIds = payments.stream()
                .map(PaymentDto::getOperationId)
                .collect(Collectors.toSet());
        paymentRepository.findAllById(operationIds).stream()
                .map(PaymentTransaction::getOperationId)
                .forEach(entityCache.getExistingOperationIds()::add);

        Set<String> clientEmails = payments.stream()
                .map(PaymentDto::getClientEmail)
                .collect(Collectors.toSet());
        clientRepository.findByEmailIn(clientEmails)
                .forEach(c -> entityCache.getClientsByEmail().put(c.getEmail(), c));

        return entityCache;
    }
}
