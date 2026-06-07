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
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @Column(name = "operation_id", unique = true)
    private Long operationId;

    @Column(name = "month")
    private Integer month;

    @Column(name = "day")
    private Integer day;

    @Column(name = "week")
    private Integer week;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "accreditation_date")
    private LocalDateTime accreditationDate;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "status")
    private String status;

    @Column(name = "operation_type", length = 100)
    private String operationType;

    @Column(name = "product_value", precision = 12, scale = 2)
    private BigDecimal productValue;

    @Column(name = "transaction_fee", precision = 12, scale = 2)
    private BigDecimal transactionFee;

    @Column(name = "amount_received", precision = 12, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "installments")
    private Integer installments;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "package", length = 100)
    private String packageName;

    @Column(name = "class_count")
    private Integer classCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public PaymentTableDto toDto() {
        return new PaymentTableDto(
                this.getOperationId(),
                this.getMonth(),
                this.getDay(),
                this.getWeek(),
                this.getPurchaseDate(),
                this.getAccreditationDate(),
                this.getReleaseDate(),
                this.getOperationType(),
                this.getProductValue(),
                this.getTransactionFee(),
                this.getAmountReceived(),
                this.getInstallments(),
                this.getPaymentMethod(),
                this.getPackageName(),
                this.getClassCount(),
                new PaymentTableDto.ClientInfo(
                        this.getClient().getName(),
                        this.getClient().getEmail(),
                        this.getClient().getPhone())
        );
    }

}