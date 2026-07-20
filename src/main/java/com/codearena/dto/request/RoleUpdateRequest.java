package com.codearena.dto.request;

import com.codearena.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {

    @NotNull
    private Role role;
}
