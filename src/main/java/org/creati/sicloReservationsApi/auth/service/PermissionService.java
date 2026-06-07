package org.creati.sicloReservationsApi.auth.service;

import org.creati.sicloReservationsApi.auth.dto.PermissionDto;
import org.creati.sicloReservationsApi.auth.model.Permission;

import java.util.List;

public interface PermissionService {
    PermissionDto createPermission(PermissionDto permissionDto);
    PermissionDto getPermissionById(Long id);
    PermissionDto getPermissionByName(String name);
    List<PermissionDto> getAllPermissions();
    PermissionDto updatePermission(Long id, PermissionDto permissionDto);
    void deletePermission(Long id);
    Permission getOrCreatePermission(PermissionDto dto);

}
