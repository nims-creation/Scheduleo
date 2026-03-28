package com.saas.Schedulo.service.user;

import com.saas.Schedulo.dto.request.user.CreateUserRequest;
import com.saas.Schedulo.dto.request.user.UpdateUserRequest;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.user.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse create(CreateUserRequest request);
    java.util.List<UserResponse> createBulk(java.util.List<CreateUserRequest> requests);
    UserResponse getById(UUID id);
    UserResponse update(UUID id, UpdateUserRequest request);
    void delete(UUID id);
    PagedResponse<UserResponse> getByOrganization(UUID organizationId, Pageable pageable);
    PagedResponse<UserResponse> search(UUID organizationId, String query, Pageable pageable);
    void updateLastLogin(UUID userId);
}
