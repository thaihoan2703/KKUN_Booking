package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.RoleDto;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    List<RoleDto> getAllRoles();
    RoleDto getRoleById(UUID id);
    RoleDto createRole(RoleDto roleDto);
    RoleDto updateRole(UUID id, RoleDto roleDto);
    void deleteRole(UUID id);
}

