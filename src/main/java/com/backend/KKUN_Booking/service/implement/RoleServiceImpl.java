package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.RoleDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Role;
import com.backend.KKUN_Booking.repository.RoleRepository;
import com.backend.KKUN_Booking.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDto getRoleById(UUID id) {
        return roleRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        Role role = convertToEntity(roleDto);
        return convertToDto(roleRepository.save(role));
    }

    @Override
    public RoleDto updateRole(UUID id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        return convertToDto(roleRepository.save(role));
    }

    @Override
    public void deleteRole(UUID id) {
        roleRepository.deleteById(id);
    }

    private RoleDto convertToDto(Role role) {
        // Chuyển đổi từ Role entity sang RoleDto
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }

    private Role convertToEntity(RoleDto roleDto) {
        // Chuyển đổi từ RoleDto sang Role entity
        Role role = new Role();
        role.setId(roleDto.getId());
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        return role;
    }
}
