package org.creati.sicloReservationsApi.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dao.PermissionRepository;
import org.creati.sicloReservationsApi.auth.dto.PermissionDto;
import org.creati.sicloReservationsApi.auth.exception.DuplicateResourceException;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.creati.sicloReservationsApi.auth.model.Permission;
import org.creati.sicloReservationsApi.auth.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(final PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public PermissionDto createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsByName(permissionDto.getName())) {
            throw new DuplicateResourceException("Permission with name " + permissionDto.getName() + " already exists.");
        }

        Permission savedPermission = permissionRepository.save(Permission.fromDto(permissionDto));
        log.info("Created permission: {}", savedPermission);
        return savedPermission.toDto();
    }

    @Override
    public PermissionDto getPermissionById(Long id) {
        Permission permissionFound = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with id " + id + " not found."));
        return permissionFound.toDto();
    }

    @Override
    public PermissionDto getPermissionByName(String name) {
        Permission permissionFound = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with name " + name + " not found."));
        return permissionFound.toDto();
    }

    @Override
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(Permission::toDto)
                .toList();
    }

    @Override
    public PermissionDto updatePermission(Long id, PermissionDto permissionDto) {
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with id " + id + " not found."));

        // Validate if the new name is different and already exists
        if (!existingPermission.getName().equals(permissionDto.getName()) && permissionRepository.existsByName(permissionDto.getName())) {
            throw new DuplicateResourceException("Permission with name " + permissionDto.getName() + " already exists.");
        }

        Permission permissionModified = existingPermission.toBuilder()
                .name(permissionDto.getName())
                .description(permissionDto.getDescription())
                .resource(permissionDto.getResource())
                .action(permissionDto.getAction())
                .build();
        Permission updated = permissionRepository.save(permissionModified);
        return updated.toDto();
    }

    @Override
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission with id " + id + " not found.");
        }
        permissionRepository.deleteById(id);
    }

    @Override
    public Permission getOrCreatePermission(PermissionDto dto) {
        return permissionRepository.findByName(dto.getName())
                .orElseGet(() -> {
                    Permission newPermission = Permission.fromDto(dto);
                    return permissionRepository.save(newPermission);
                });
    }

}
