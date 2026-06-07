package org.creati.sicloReservationsApi.auth.service;

import org.creati.sicloReservationsApi.auth.dto.RoleDto;
import org.creati.sicloReservationsApi.auth.model.Role;

import java.util.List;
import java.util.Set;

public interface RoleService {

    RoleDto createRole(RoleDto roleDto);
    RoleDto getRoleById(Long id);
    RoleDto getRoleByName(String name);
    List<RoleDto> getAllRoles(String iod);
    RoleDto updateRole(Long id, RoleDto roleDto);
    void deleteRole(Long id);

    RoleDto addPermissionsToRole(Long roleId, Set<Long> permissionId);
    RoleDto removePermissionsFromRole(Long roleId, Set<Long> permissionId);


}
