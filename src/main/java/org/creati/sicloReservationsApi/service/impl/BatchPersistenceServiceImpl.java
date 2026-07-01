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
import org.creati.sicloReservationsApi.service.BatchPersistenceService;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
    @Transactional
    public ProcessingResult persistReservationsBatch(List<ReservationDto> reservationDtoList, EntityCache cache) {
        return persistBatch(
                reservationDtoList,
                ReservationDto::getReservationId,
                cache.getExistingReservationIds(),
                dto -> buildReservationEntity(dto, cache),
                reservationRepository);
    }

    @Override
    @Transactional
    public ProcessingResult persistPaymentsBatch(List<PaymentDto> paymentDtoList, EntityCache cache) {
        return persistBatch(
                paymentDtoList,
                PaymentDto::getOperationId,
                cache.getExistingOperationIds(),
                dto -> buildPaymentEntity(dto, cache),
                paymentRepository);
    }

    /**
     * Shared batch loop: skips rows whose id is already present, builds an
     * entity for each new row (collecting per-row errors), then persists the
     * surviving entities in one {@code saveAll}.
     */
    private <D, E> ProcessingResult persistBatch(
            List<D> dtoList,
            Function<D, Long> idExtractor,
            Set<Long> existingIds,
            Function<D, E> entityBuilder,
            JpaRepository<E, Long> repository) {

        List<String> errors = new ArrayList<>();
        List<E> toSave = new ArrayList<>();
        int processedRows = 0;
        int errorRows = 0;
        int skippedRows = 0;

        for (int i = 0; i < dtoList.size(); i++) {
            D dto = dtoList.get(i);
            try {
                if (existingIds.contains(idExtractor.apply(dto))) {
                    log.debug("Skipping existing id: {}", idExtractor.apply(dto));
                    skippedRows++;
                    continue;
                }
                toSave.add(entityBuilder.apply(dto));
                processedRows++;
            } catch (Exception e) {
                errorRows++;
                errors.add(String.format("Error processing row %d: %s", i + 1, e.getMessage()));
                log.error("Error processing row {}: exception: {}", i + 1, e.getMessage());
            }
        }

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
            log.info("Saved {} new records", toSave.size());
        }

        return ProcessingResult.builder()
                .success(errorRows == 0)
                .totalProcessed(dtoList.size())
                .successCount(processedRows)
                .failureCount(errorRows)
                .skipped(skippedRows)
                .errors(errors)
                .build();
    }

    /**
     * Returns the cached entity for {@code key}, or builds, persists and caches
     * a new one when absent.
     */
    private <K, V> V getOrCreate(Map<K, V> cache, K key, Function<K, V> builder, JpaRepository<V, Long> repository) {
        return cache.computeIfAbsent(key, k -> {
            log.debug("Creating new entity for key: {}", k);
            return repository.save(builder.apply(k));
        });
    }


    private Reservation buildReservationEntity(ReservationDto dto, EntityCache cache) {
        Client client = getOrCreate(cache.getClientsByEmail(), dto.getClientEmail(),
                email -> Client.builder().email(email).build(),
                clientRepository);

        Studio studio = getOrCreate(cache.getStudiosByName(), dto.getStudioName(),
                name -> Studio.builder()
                        .name(name)
                        .country(dto.getCountry())
                        .city(dto.getCity())
                        .build(),
                studioRepository);

        String roomKey = studio.getName() + "|" + dto.getRoomName();
        Room room = getOrCreate(cache.getRoomsByStudioAndName(), roomKey,
                key -> Room.builder()
                        .name(dto.getRoomName())
                        .studio(studio)
                        .build(),
                roomRepository);

        Discipline discipline = getOrCreate(cache.getDisciplinesByName(), dto.getDisciplineName(),
                name -> Discipline.builder().name(name).build(),
                disciplineRepository);

        Instructor instructor = getOrCreate(cache.getInstructorsByName(), dto.getInstructorName(),
                name -> Instructor.builder().name(name).build(),
                instructorRepository);

        return Reservation.builder()
                .reservationId(dto.getReservationId())
                .classId(dto.getClassId())
                .room(room)
                .discipline(discipline)
                .instructor(instructor)
                .client(client)
                .reservationDate(dto.getDay())
                .reservationTime(dto.getTime())
                .orderCreator(dto.getOrderCreator())
                .paymentMethod(dto.getPaymentMethod())
                .status(dto.getStatus())
                .build();
    }


    private PaymentTransaction buildPaymentEntity(PaymentDto dto, EntityCache cache) {
        Client client = getOrCreate(cache.getClientsByEmail(), dto.getClientEmail(),
                email -> Client.builder()
                        .phone(dto.getPhone())
                        .documentId(dto.getDocumentId())
                        .email(email)
                        .build(),
                clientRepository);

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
                .client(client)
                .build();
    }


}
