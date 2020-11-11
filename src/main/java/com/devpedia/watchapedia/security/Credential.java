package com.devpedia.watchapedia.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Credential implements UserDetails {

    private Long id;
    private List<String> roles;

    public Credential(Long id, List<String> roles) {
        this.id = id;
        this.roles = roles;
    }

    public static Credential of(JwtTokenProvider.JwtParseInfo parseInfo) {
        return new Credential(parseInfo.getId(), parseInfo.getRoles());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        // UserDetailsService를 구현하지 않고
        // 즉, DB를 거치지 않을 경우 필요하지 않은 것으로 판단.
        // 현재는 토큰에 담겨오는 정보만 이용하기 때문에 구현하지 않는다.
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
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
