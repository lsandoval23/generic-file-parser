package org.creati.sicloReservationsApi.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {

    @NotNull(message = "ID de reserva es obligatorio")
    private Long reservationId;

    @NotNull(message = "ID de clase es obligatorio")
    private Long classId;

    @NotBlank(message = "País es obligatorio")
    private String country;

    @NotBlank(message = "Ciudad es obligatoria")
    private String city;

    @NotBlank(message = "Disciplina es obligatoria")
    private String disciplineName;

    @NotBlank(message = "Estudio es obligatorio")
    private String studioName;

    @NotBlank(message = "Salón es obligatorio")
    private String roomName;

    @NotBlank(message = "Instructor es obligatorio")
    private String instructorName;

    @NotNull(message = "Día es obligatorio")
    private LocalDate day;

    @NotNull(message = "Hora es obligatoria")
    private LocalTime time;

    @NotBlank(message = "Cliente es obligatorio")
    private String clientEmail;

    private String orderCreator;
    private String paymentMethod;
    private String status;


}
