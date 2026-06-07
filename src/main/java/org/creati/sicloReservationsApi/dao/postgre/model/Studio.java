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

@Entity
@Table(name = "studio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Studio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "studio_id")
    private Long studioId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
