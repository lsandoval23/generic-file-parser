package org.creati.sicloReservationsApi.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.auth.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class UserDto {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Set<RoleDto> roles;
    private Boolean isActive;


    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles().stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
    }

}
