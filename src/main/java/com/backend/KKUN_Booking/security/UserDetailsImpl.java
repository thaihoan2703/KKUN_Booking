package com.backend.KKUN_Booking.security;

import com.backend.KKUN_Booking.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserDetailsImpl implements UserDetails {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    // Constructor dùng để chuyển đổi từ User sang UserDetailsImpl
    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole().getName();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName()));
    }

    // Tạo phương thức static để tiện tạo đối tượng từ User
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
