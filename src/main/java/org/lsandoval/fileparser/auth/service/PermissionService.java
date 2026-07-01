package org.lsandoval.fileparser.auth.service;

import org.lsandoval.fileparser.auth.dto.PermissionDto;
import org.lsandoval.fileparser.auth.model.Permission;

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
