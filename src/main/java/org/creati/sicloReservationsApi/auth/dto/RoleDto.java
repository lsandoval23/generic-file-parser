package org.creati.sicloReservationsApi.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.auth.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class RoleDto {

    private Long id;
    private String name;
    private String description;
    private Set<PermissionDto> permissions;

    public RoleDto(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream()
                .map(PermissionDto::new)
                .collect(Collectors.toSet());
    }
}
