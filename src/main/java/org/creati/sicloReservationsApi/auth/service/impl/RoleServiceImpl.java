package org.creati.sicloReservationsApi.auth.service.impl;

import org.creati.sicloReservationsApi.auth.dao.RoleRepository;
import org.creati.sicloReservationsApi.auth.dto.RoleDto;
import org.creati.sicloReservationsApi.auth.exception.DuplicateResourceException;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.creati.sicloReservationsApi.auth.model.Permission;
import org.creati.sicloReservationsApi.auth.model.Role;
import org.creati.sicloReservationsApi.auth.service.PermissionService;
import org.creati.sicloReservationsApi.auth.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    public RoleServiceImpl(
            final RoleRepository roleRepository,
            final PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionService = permissionService;
    }


    @Override
    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new DuplicateResourceException("Role with name " + roleDto.getName() + " already exists");
        }

        Role role = Role.fromDto(roleDto);
        Role savedRole = roleRepository.save(role);
        return savedRole.toDto();
    }

    @Override
    public RoleDto getRoleById(Long id) {
        Role foundRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + id + " not found"));
        return foundRole.toDto();
    }

    @Override
    public RoleDto getRoleByName(String name) {
        Role foundRole = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role with name " + name + " not found"));
        return foundRole.toDto();
    }

    @Override
    public List<RoleDto> getAllRoles(String iod) {
        return Optional.ofNullable(iod)
                .filter(param -> param.equals("permissions"))
                .map(param -> roleRepository.findAll().stream()
                        .map(Role::toDto)
                        .toList())
                .orElse(roleRepository.findAll().stream()
                        .map(Role::toDtoWithoutPermissions)
                        .toList());
    }

    @Override
    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + id + " not found"));

        if (!existingRole.getName().equals(roleDto.getName()) && roleRepository.existsByName(roleDto.getName())) {
            throw new DuplicateResourceException("Role with name " + roleDto.getName() + " already exists");
        }

        Role modifiedRole = existingRole.toBuilder()
                .name(roleDto.getName())
                .description(roleDto.getDescription())
                .permissions(Optional.ofNullable(roleDto.getPermissions())
                        .map(permissionList -> permissionList.stream()
                                .map(Permission::fromDto)
                                .collect(Collectors.toSet()))
                        .orElse(null))
                .build();

        Role updatedRole = roleRepository.save(modifiedRole);
        return updatedRole.toDto();
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role with id " + id + " not found");
        }
        roleRepository.deleteById(id);
    }

    @Override
    public RoleDto addPermissionsToRole(Long roleId, Set<Long> permissionId) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + roleId + " not found"));
        permissionId.forEach(id -> {
            Permission permission = Permission.fromDto(permissionService.getPermissionById(id));
            existingRole.getPermissions().add(permission);
        });
        Role updatedRole = roleRepository.save(existingRole);
        return updatedRole.toDto();
    }

    @Override
    public RoleDto removePermissionsFromRole(Long roleId, Set<Long> permissionId) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + roleId + " not found"));
        existingRole.getPermissions()
                .removeIf(p -> permissionId.contains(p.getId()));
        Role updatedRole = roleRepository.save(existingRole);
        return updatedRole.toDto();
    }
}
