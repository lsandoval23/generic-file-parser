package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "order_creator", length = 150)
    private String orderCreator;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_id", nullable = false)
    private Discipline discipline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public ReservationTableDto toDto() {
        return new ReservationTableDto(
                this.getReservationId(),
                this.getClassId(),
                this.getReservationDate(),
                this.getReservationTime(),
                this.getOrderCreator(),
                this.getPaymentMethod(),
                this.getStatus(),
                new ReservationTableDto.ClientInfo(
                        this.getClient().getName(),
                        this.getClient().getEmail(),
                        this.getClient().getPhone()
                ),
                new ReservationTableDto.LocationInfo(
                        this.getRoom().getStudio().getName(),
                        this.getRoom().getName(),
                        this.getRoom().getStudio().getCountry(),
                        this.getRoom().getStudio().getCity()
                ),
                this.getDiscipline().getName(),
                this.getInstructor().getName()
        );
    }


}