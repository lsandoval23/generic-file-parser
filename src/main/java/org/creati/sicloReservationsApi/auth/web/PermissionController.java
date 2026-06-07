package org.creati.sicloReservationsApi.auth.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dto.PermissionDto;
import org.creati.sicloReservationsApi.auth.service.PermissionService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/permissions")
@PreAuthorize("hasAuthority('MANAGE_PERMISSIONS')")
@RestController
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        log.info("GET /permissions - Fetching all permissions");
        List<PermissionDto> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @PostMapping("")
    public ResponseEntity<PermissionDto> createPermission(@RequestBody @Valid PermissionDto permissionDto) {
        log.info("POST /permissions - Creating new permission");
        PermissionDto createdPermission = permissionService.createPermission(permissionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionDto> getPermissionById(@PathVariable Long id) {
        log.info("GET /permissions/{} - Fetching permission by ID", id);
        PermissionDto permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<PermissionDto> getPermissionByName(@PathVariable String name) {
        log.info("GET /permissions/name/{} - Fetching permission by name", name);
        PermissionDto permission = permissionService.getPermissionByName(name);
        return ResponseEntity.ok(permission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionDto> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionDto permissionDto
    ) {
        log.info("PUT /permissions/{} - Updating permission", id);
        PermissionDto updatedPermission = permissionService.updatePermission(id, permissionDto);
        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        log.info("DELETE /permissions/{} - Deleting permission", id);
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

}
