package com.saas.Schedulo.service.impl.user;

import com.saas.Schedulo.dto.mapper.UserMapper;
import com.saas.Schedulo.dto.request.user.CreateUserRequest;
import com.saas.Schedulo.dto.request.user.UpdateUserRequest;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.user.UserResponse;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.user.Role;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.exception.resource.ResourceConflictException;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.user.RoleRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResourceConflictException("User with this email already exists");
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        User user = userMapper.toEntity(request);
        user.setOrganization(organization);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().toLowerCase());

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
                roles.add(role);
            }
        } else {
            // Best practice: fallback to ROLE_USER
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
            roles.add(defaultRole);
        }
        user.setRoles(roles);

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userMapper.updateUserFromRequest(request, user);

        if (request.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("User soft deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getByOrganization(UUID organizationId, Pageable pageable) {
        Page<User> userPage = userRepository.findByOrganizationId(organizationId, pageable);
        return PagedResponse.from(userPage.map(userMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> search(UUID organizationId, String query, Pageable pageable) {
        Page<User> userPage = userRepository.searchByOrganization(organizationId, query, pageable);
        return PagedResponse.from(userPage.map(userMapper::toResponse));
    }

    @Override
    public void updateLastLogin(UUID userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }
}
