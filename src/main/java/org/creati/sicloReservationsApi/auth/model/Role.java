package org.creati.sicloReservationsApi.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.creati.sicloReservationsApi.auth.dto.RoleDto;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public RoleDto toDto() {
        return RoleDto.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .permissions(Optional.ofNullable(this.permissions)
                        .map(permissionSet -> permissionSet.stream()
                                .map(Permission::toDto)
                                .collect(Collectors.toSet()))
                        .orElse(null))
                .build();
    }

    public RoleDto toDtoWithoutPermissions() {
        return RoleDto.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .build();
    }

    public static Role fromDto(RoleDto dto) {
        return Role.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .permissions(Optional.ofNullable(dto.getPermissions())
                        .map(permissionDtoSet -> permissionDtoSet.stream()
                                .map(Permission::fromDto)
                                .collect(Collectors.toSet()))
                        .orElse(null))
                .build();
    }



}
