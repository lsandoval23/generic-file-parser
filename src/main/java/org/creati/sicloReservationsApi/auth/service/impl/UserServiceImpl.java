package org.creati.sicloReservationsApi.auth.service.impl;

import org.creati.sicloReservationsApi.auth.dao.UserRepository;
import org.creati.sicloReservationsApi.auth.dto.UserDto;
import org.creati.sicloReservationsApi.auth.exception.DuplicateResourceException;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.creati.sicloReservationsApi.auth.model.Role;
import org.creati.sicloReservationsApi.auth.model.User;
import org.creati.sicloReservationsApi.auth.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + userDto.getEmail());
        }

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("Username already in use: " + userDto.getUsername());
        }

        User user = User.fromDto(userDto, passwordEncoder);
        User savedUser = userRepository.save(user);
        return savedUser.toDto();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return user.toDto();
    }

    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with username: " + username));
        return user.toDto();
    }

    @Override
    public List<UserDto> getAllUsers(String iod) {
        return Optional.ofNullable(iod)
                .filter(param -> param.equals("roles"))
                .map(param -> userRepository.findAll().stream()
                        .map(User::toDto)
                        .toList())
                .orElse(userRepository.findAll().stream()
                        .map(User::toDtoWithoutRoles)
                        .toList());
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        // Look for existing email and username
        if (!existingUser.getEmail().equals(userDto.getEmail()) && userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + userDto.getEmail());
        }

        if (!existingUser.getUsername().equals(userDto.getUsername()) && userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("Username already in use: " + userDto.getUsername());
        }

        User.UserBuilder modifiedUserBuilder = existingUser.toBuilder();

        // Only encode and update the password if it has changed
        if ( userDto.getPassword()!= null && !existingUser.getPassword().equals(userDto.getPassword())) {
            modifiedUserBuilder.password(passwordEncoder.encode(userDto.getPassword()));
        }

        User modifiedUser = modifiedUserBuilder
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .isActive(userDto.getIsActive())
                .roles(Optional.ofNullable(userDto.getRoles())
                        .map(roleList -> roleList.stream()
                                .map(Role::fromDto)
                                .collect(Collectors.toSet()))
                        .orElse(null))
                .build();

        User updated = userRepository.save(modifiedUser);
        return updated.toDto();
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto addRolesToUser(String username, Set<Long> roleIds) {
        return null;
    }

    @Override
    public UserDto removeRolesFromUser(String username, Set<Long> roleIds) {
        return null;
    }
}
