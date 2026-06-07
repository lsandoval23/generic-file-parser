package org.creati.sicloReservationsApi.auth.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dto.UserDto;
import org.creati.sicloReservationsApi.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@PreAuthorize("hasAuthority('MANAGE_USER')")
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(value = "iod", required = false) String iod
    ) {
        log.info("GET /api/users - Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers(iod));
    }

    @PostMapping("")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Fetching user", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        log.info("GET /api/users/username/{} - Fetching user by username", username);
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto) {
        log.info("PUT /api/users/{} - Updating user", id);
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
