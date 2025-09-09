package com.yield.barbershop_backend.config.security.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.yield.barbershop_backend.service.UserService;


@Component
public class UserAuthenticationProvider implements AuthenticationProvider  {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserAuthenticationProvider() {
    }


    @Override
    public Authentication authenticate(Authentication authentication) {

        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        System.out.println("Attempting authentication for email: " + email + ", password: " + password);

        UserDetails userDetails = userService.loadUserByEmail(email);

        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new UsernameNotFoundException("Invalid password");
        }
        
        return new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
    
}
