package com.yield.barbershop_backend.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.ToString;


@ToString
public class AccountPrincipal<T extends AccountInterface> implements UserDetails {

    private T account;

    public AccountPrincipal(T account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(account.getRole()::toString);
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return account.getEmail();
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
        return account.getIsActive() == true;
    }

    public Long getId() {
        return account.getId();
    }

    public String getEmail() {
        return account.getEmail();
    }

     

}
