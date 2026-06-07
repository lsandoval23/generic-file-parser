package org.creati.sicloReservationsApi.auth.service;

import org.creati.sicloReservationsApi.auth.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserDto createUser(UserDto userDto);
    UserDto getUserById(Long id);
    UserDto getUserByUsername(String username);
    List<UserDto> getAllUsers(String iod);
    UserDto updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    UserDto addRolesToUser(String username, Set<Long> roleIds);
    UserDto removeRolesFromUser(String username, Set<Long> roleIds);

}
