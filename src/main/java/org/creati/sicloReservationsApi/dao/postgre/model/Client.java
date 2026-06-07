package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "client")
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "document_id", length = 50)
    private String documentId;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentTransaction> paymentTransactions;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

}
