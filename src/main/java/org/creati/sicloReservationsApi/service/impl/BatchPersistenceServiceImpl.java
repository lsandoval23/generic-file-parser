package org.creati.sicloReservationsApi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.DisciplineRepository;
import org.creati.sicloReservationsApi.dao.postgre.InstructorRepository;
import org.creati.sicloReservationsApi.dao.postgre.PaymentTransactionRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.RoomRepository;
import org.creati.sicloReservationsApi.dao.postgre.StudioRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.creati.sicloReservationsApi.dao.postgre.model.Instructor;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.creati.sicloReservationsApi.dao.postgre.model.Studio;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.creati.sicloReservationsApi.service.BatchPersistenceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BatchPersistenceServiceImpl implements BatchPersistenceService {

    private final ReservationRepository reservationRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final StudioRepository studioRepository;
    private final RoomRepository roomRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;

    public BatchPersistenceServiceImpl(
            final ReservationRepository reservationRepository,
            final PaymentTransactionRepository paymentRepository,
            final ClientRepository clientRepository,
            final StudioRepository studioRepository,
            final RoomRepository roomRepository,
            final DisciplineRepository disciplineRepository,
            final InstructorRepository instructorRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
        this.studioRepository = studioRepository;
        this.roomRepository = roomRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
    }


    @Override
    public ProcessingResult persistReservationsBatch(List<ReservationDto> reservationDtoList, EntityCache cache) {

        List<String> errors = new ArrayList<>();
        List<Reservation> reservationsToSave = new ArrayList<>();

        int processedRows = 0;
        int errorRows = 0;
        int skippedRows = 0;

        for (int i = 0; i < reservationDtoList.size(); i++) {
            ReservationDto reservation = reservationDtoList.get(i);
            try {
                if (cache.getExistingReservationIds().contains(reservation.getReservationId())) {
                    log.warn("Skipping existing reservation ID: {}", reservation.getReservationId());
                    skippedRows++;
                    continue;
                }
                Reservation reservationEntity = buildReservationEntity(reservation, cache);
                reservationsToSave.add(reservationEntity);
                processedRows++;
            } catch (Exception e) {
                errorRows++;
                errors.add(String.format("Error processing row %d: %s", i + 1, e.getMessage()));
                log.error("Error processing reservation at row {}: exception: {}", i + 1, e.getMessage());
            }
        }

        if (!reservationsToSave.isEmpty()) {
            reservationRepository.saveAll(reservationsToSave);
            log.info("Saved {} new reservations", reservationsToSave.size());
        }

        return ProcessingResult.builder()
                .success(errorRows == 0)
                .totalProcessed(reservationDtoList.size())
                .successCount(processedRows)
                .failureCount(errorRows)
                .skipped(skippedRows)
                .errors(errors)
                .build();

    }

    @Override
    public ProcessingResult persistPaymentsBatch(List<PaymentDto> paymentDtoList, EntityCache cache) {

        List<String> errors = new ArrayList<>();
        List<PaymentTransaction> paymentsToSave = new ArrayList<>();

        int processedRows = 0;
        int errorRows = 0;
        int skippedRows = 0;

        for (int i = 0; i < paymentDtoList.size(); i++) {
            PaymentDto payment = paymentDtoList.get(i);
            try {
                if (cache.getExistingOperationIds().contains(payment.getOperationId())) {
                    log.debug("Skipping existing operation ID: {}", payment.getOperationId());
                    skippedRows++;
                    continue;
                }

                PaymentTransaction paymentEntity = buildPaymentEntity(payment, cache);
                paymentsToSave.add(paymentEntity);
                processedRows++;
            } catch (Exception e) {
                errorRows++;
                errors.add(String.format("Error processing row %d: %s", i + 1, e.getMessage()));
                log.error("Error processing payment at row {}: exception: {}", i + 1, e.getMessage());
            }
        }

        if (!paymentsToSave.isEmpty()) {
            paymentRepository.saveAll(paymentsToSave);
            log.info("Saved {} new payments", paymentsToSave.size());
        }

        return ProcessingResult.builder()
                .success(errorRows == 0)
                .totalProcessed(paymentDtoList.size())
                .successCount(processedRows)
                .failureCount(errorRows)
                .skipped(skippedRows)
                .errors(errors)
                .build();

    }


    private Reservation buildReservationEntity(ReservationDto dto, EntityCache cache) {
        Client newClient = cache.getClientsByEmail().computeIfAbsent(dto.getClientEmail(), email -> {
            log.debug("Creating new client for email: {}", email);
            Client client = Client.builder()
                    .email(email)
                    .build();
            return clientRepository.save(client);
        });

        Studio newStudio = cache.getStudiosByName().computeIfAbsent(dto.getStudioName(), name -> {
            log.debug("Creating new studio for name: {}", name);
            Studio studio = Studio.builder()
                    .name(name)
                    .country(dto.getCountry())
                    .city(dto.getCity())
                    .build();
            return studioRepository.save(studio);
        });

        String roomKey = newStudio.getName() + "|" + dto.getRoomName();
        Room newRoom = cache.getRoomsByStudioAndName().computeIfAbsent(roomKey, key -> {
            log.debug("Creating new room: {} for studio: {}", dto.getRoomName(), newStudio.getStudioId());
            Room room = Room.builder()
                    .name(dto.getRoomName())
                    .studio(newStudio)
                    .build();
            return roomRepository.save(room);
        });

        Discipline newDiscipline = cache.getDisciplinesByName().computeIfAbsent(dto.getDisciplineName(), name -> {
            log.debug("Creating new discipline for name: {}", name);
            Discipline discipline = Discipline.builder()
                    .name(name)
                    .build();
            return disciplineRepository.save(discipline);
        });

        Instructor newInstructor = cache.getInstructorsByName().computeIfAbsent(dto.getInstructorName(), name -> {
            log.debug("Creating new instructor for name: {}", name);
            Instructor instructor = Instructor.builder()
                    .name(name)
                    .build();
            return instructorRepository.save(instructor);
        });

        return Reservation.builder()
                .reservationId(dto.getReservationId())
                .classId(dto.getClassId())
                .room(newRoom)
                .discipline(newDiscipline)
                .instructor(newInstructor)
                .client(newClient)
                .reservationDate(dto.getDay())
                .reservationTime(dto.getTime())
                .orderCreator(dto.getOrderCreator())
                .paymentMethod(dto.getPaymentMethod())
                .status(dto.getStatus())
                .build();
    }


    private PaymentTransaction buildPaymentEntity(PaymentDto dto, EntityCache cache) {

        Client newClient = cache.getClientsByEmail().computeIfAbsent(dto.getClientEmail(), email -> {
            log.debug("Creating new client for email: {}", email);
            Client client = Client.builder()
                    .phone(dto.getPhone())
                    .documentId(dto.getDocumentId())
                    .email(email)
                    .build();
            return clientRepository.save(client);
        });

        return PaymentTransaction.builder()
                .operationId(dto.getOperationId())
                .month(dto.getMonth())
                .day(dto.getDay())
                .week(dto.getWeek())
                .purchaseDate(dto.getPurchaseDate())
                .accreditationDate(dto.getAccreditationDate())
                .releaseDate(dto.getReleaseDate())
                .status(dto.getStatus())
                .operationType(dto.getOperationType())
                .productValue(dto.getProductValue())
                .transactionFee(dto.getTransactionFee())
                .amountReceived(dto.getAmountReceived())
                .installments(dto.getInstallments())
                .paymentMethod(dto.getPaymentMethod())
                .packageName(dto.getPackageName())
                .classCount(dto.getClassCount())
                .client(newClient)
                .build();
    }


}
