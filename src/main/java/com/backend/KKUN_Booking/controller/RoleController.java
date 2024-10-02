package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.RoleDto;
import com.backend.KKUN_Booking.service.RoleService;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping(value = "", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public RoleDto getRoleById(@PathVariable UUID id) {
        return roleService.getRoleById(id);
    }

    @PostMapping
    public RoleDto createRole(@RequestBody RoleDto roleDto) {
        return roleService.createRole(roleDto);
    }

    @PutMapping("/{id}")
    public RoleDto updateRole(@PathVariable UUID id, @RequestBody RoleDto roleDto) {
        return roleService.updateRole(id, roleDto);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
    }
}

