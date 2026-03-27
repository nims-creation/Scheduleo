package com.saas.Schedulo.dto.request.organization;

import com.saas.Schedulo.entity.organization.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required")
    private Resource.ResourceType type;

    private Integer capacity;

    @Builder.Default
    private Boolean isAvailableForBooking = true;
}
