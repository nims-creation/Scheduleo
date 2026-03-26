package com.saas.Schedulo.dto.mapper;

import com.saas.Schedulo.dto.request.user.CreateUserRequest;
import com.saas.Schedulo.dto.request.user.UpdateUserRequest;
import com.saas.Schedulo.dto.response.user.UserResponse;
import com.saas.Schedulo.entity.user.Role;
import com.saas.Schedulo.entity.user.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "organization", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
