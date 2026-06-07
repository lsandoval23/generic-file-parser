package org.creati.sicloReservationsApi.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.creati.sicloReservationsApi.auth.dto.PermissionDto;

import java.time.Instant;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "resource")
    private String resource;

    @Column(name = "action")
    private String action;

    @Column(name = "created_at")
    private Instant createdAt;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public PermissionDto toDto() {
        return PermissionDto.builder()
                .id(this.getId())
                .name(this.getName())
                .description(this.getDescription())
                .resource(this.getResource())
                .action(this.getAction())
                .build();
    }

    public static Permission fromDto(PermissionDto dto) {
        return Permission.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .resource(dto.getResource())
                .action(dto.getAction())
                .build();
    }

}
