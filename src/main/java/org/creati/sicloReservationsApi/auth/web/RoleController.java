package org.creati.sicloReservationsApi.auth.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dto.RoleDto;
import org.creati.sicloReservationsApi.auth.service.RoleService;
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
import java.util.Set;

@Slf4j
@RequestMapping("/roles")
@PreAuthorize("hasAuthority('MANAGE_ROLES')")
@RestController
public class RoleController {

    private final RoleService roleService;

    public RoleController(final RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("")
    public ResponseEntity<List<RoleDto>> getAllRoles(
            @RequestParam(value = "iod", required = false) String iod
    ) {
        log.info("GET /roles - Fetching all roles");
        return ResponseEntity.ok(roleService.getAllRoles(iod));
    }

    @PostMapping("")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        log.info("POST /roles - Creating new role");
        RoleDto createdRole = roleService.createRole(roleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        log.info("GET /roles/{} - Fetching role by ID", id);
        RoleDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<RoleDto> getRoleByName(@PathVariable String name) {
        log.info("GET /roles/name/{} - Fetching role by name", name);
        RoleDto role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleDto roleDto
    ) {
        log.info("PUT /roles/{} - Updating role", id);
        RoleDto updatedRole = roleService.updateRole(id, roleDto);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("DELETE /roles/{} - Deleting role", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roleId}/permissions/add")
    public ResponseEntity<RoleDto> addPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody Set<Long> permissionIds
    ) {
        log.info("PUT /roles/{}/permissions - Adding permissions to role", roleId);
        RoleDto updatedRole = roleService.addPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok(updatedRole);
    }

    @PutMapping("/{roleId}/permissions/remove")
    public ResponseEntity<RoleDto> removePermissionsFromRole(
            @PathVariable Long roleId,
            @RequestBody Set<Long> permissionIds
    ) {
        log.info("PUT /roles/{}/permissions/remove - Removing permissions from role", roleId);
        RoleDto updatedRole = roleService.removePermissionsFromRole(roleId, permissionIds);
        return ResponseEntity.ok(updatedRole);
    }

}
