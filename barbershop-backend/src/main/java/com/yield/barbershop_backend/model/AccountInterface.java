package com.yield.barbershop_backend.model;

public interface AccountInterface {
    public Long getId();
    public String getEmail();
    public String getPassword();
    public Boolean getIsActive();
    public String getRole();
}
