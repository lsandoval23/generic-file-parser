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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.creati.sicloReservationsApi.auth.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UserDto toDto() {
        return UserDto.builder()
                .id(this.getId())
                .username(this.getUsername())
                .email(this.getEmail())
                .firstName(this.getFirstName())
                .lastName(this.getLastName())
                .roles(Optional.ofNullable(this.getRoles())
                        .map(roleList -> roleList.stream()
                                .map(Role::toDto)
                                .collect(Collectors.toSet()))
                        .orElse(null))
                .isActive(this.isActive)
                .build();
    }

    public UserDto toDtoWithoutRoles() {
        return UserDto.builder()
                .id(this.getId())
                .username(this.getUsername())
                .email(this.getEmail())
                .firstName(this.getFirstName())
                .lastName(this.getLastName())
                .isActive(this.isActive)
                .build();
    }


    public static User fromDto(UserDto dto, PasswordEncoder passwordEncoder) {
        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .roles(Optional.ofNullable(dto.getRoles())
                        .map(roleDtoSet -> roleDtoSet.stream()
                                .map(Role::fromDto)
                                .collect(Collectors.toSet()))
                        .orElse(null))
                .isActive(dto.getIsActive())
                .build();
    }


}
