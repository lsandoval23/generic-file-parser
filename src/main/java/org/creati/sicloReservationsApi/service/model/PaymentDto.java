package org.creati.sicloReservationsApi.service.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {

    private Integer month;
    private Integer day;
    private Integer week;
    private LocalDateTime purchaseDate;
    private LocalDateTime accreditationDate;
    private LocalDateTime releaseDate;

    @NotBlank(message = "Email es obligatorio")
    private String clientEmail;

    private String phone;
    private String documentId;
    private Long operationId;
    private String status;
    private String operationType;
    private BigDecimal productValue;
    private BigDecimal transactionFee;
    private BigDecimal amountReceived;
    private Integer installments;
    private String paymentMethod;
    private String packageName;
    private Integer classCount;
}
