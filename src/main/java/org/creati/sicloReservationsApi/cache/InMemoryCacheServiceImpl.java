package org.creati.sicloReservationsApi.cache;

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

        // Preload clients by email
        Set<String> clientEmails = reservations.stream()
                .map(ReservationDto::getClientEmail)
                .collect(Collectors.toSet());

        clientRepository.findAll().forEach(client -> {
            if (clientEmails.contains(client.getEmail())) {
                entityCache.getClientsByEmail().put(client.getEmail(), client);
            }
        });

        // Preload studios
        Set<String> studioNames = reservations.stream()
                .map(ReservationDto::getStudioName)
                .collect(Collectors.toSet());

        studioRepository.findAll().forEach(studio -> {
            if (studioNames.contains(studio.getName())) {
                entityCache.getStudiosByName().put(studio.getName(), studio);
            }
        });

        // Preload disciplines
        Set<String> disciplineNames = reservations.stream()
                .map(ReservationDto::getDisciplineName)
                .collect(Collectors.toSet());
        disciplineRepository.findAll().forEach(discipline -> {
            if (disciplineNames.contains(discipline.getName())) {
                entityCache.getDisciplinesByName().put(discipline.getName(), discipline);
            }
        });

        // Preload instructors
        Set<String> instructorNames = reservations.stream()
                .map(ReservationDto::getInstructorName)
                .collect(Collectors.toSet());
        instructorRepository.findAll().forEach(instructor -> {
            if (instructorNames.contains(instructor.getName())) {
                entityCache.getInstructorsByName().put(instructor.getName(), instructor);
            }
        });

        // Preload rooms
        roomRepository.findAll().forEach(room -> {
            String key = room.getStudio().getName() + "|" + room.getName();
            entityCache.getRoomsByStudioAndName().put(key, room);
        });

        // Preload existing reservations by reservationId
        Set<Long> reservationIds = reservations.stream()
                .map(ReservationDto::getReservationId)
                .collect(Collectors.toSet());
        entityCache.getExistingReservationIds().addAll(reservationRepository.findAllById(reservationIds)
                .stream()
                .map(Reservation::getReservationId)
                .collect(Collectors.toSet()));

        return entityCache;
    }

    @Override
    public EntityCache preloadEntitiesForPayments(List<PaymentDto> payments) {

        EntityCache entityCache = new EntityCache();

        // Preload payment operation IDs
        Set<Long> operationIds = payments.stream()
                .map(PaymentDto::getOperationId)
                .collect(Collectors.toSet());
        entityCache.getExistingOperationIds().addAll(paymentRepository.findAllById(operationIds)
                .stream()
                .map(PaymentTransaction::getOperationId)
                .collect(Collectors.toSet()));


        // Preload clients by email
        Set<String> clientEmails = payments.stream()
                .map(PaymentDto::getClientEmail)
                .collect(Collectors.toSet());

        clientRepository.findAll().forEach(client -> {
            if (clientEmails.contains(client.getEmail())) {
                entityCache.getClientsByEmail().put(client.getEmail(), client);
            }
        });

        return entityCache;
    }
}
