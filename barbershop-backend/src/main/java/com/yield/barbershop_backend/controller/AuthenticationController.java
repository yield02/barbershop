package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.config.security.providers.CustomerAuthenticationProvider;
import com.yield.barbershop_backend.config.security.providers.UserAuthenticationProvider;
import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.LoginDTO;
import com.yield.barbershop_backend.dto.TokenDataEntity;
import com.yield.barbershop_backend.model.AccountPrincipal;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.service.RefreshTokenService;
import com.yield.barbershop_backend.util.JwtUtil;

import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private CustomerAuthenticationProvider customerAuthenticationProvider;
    
    @Autowired
    private UserAuthenticationProvider userAuthenicationProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login/customer")
    public ResponseEntity<ApiResponse<Void>> LoginCustomer(@RequestBody @Validated LoginDTO loginDTO, HttpServletResponse response) {
        

        Authentication authentication = customerAuthenticationProvider.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        if(!authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "Authentication failed", null));
        }

        AccountPrincipal<Customer> principal = (AccountPrincipal<Customer>) authentication.getPrincipal();

        // Access Token generation
        TokenDataEntity tokenData = new TokenDataEntity();
        tokenData.setId(principal.getId());
        tokenData.setType(TokenDataEntity.Type.CUSTOMER);
        tokenData.setEmail(principal.getEmail());
        tokenData.setRole(principal.getAuthorities());

        String accessToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.ACCESS); // 15 minutes
        String refreshToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.REFRESH); // 30 days

        jwtUtil.setAuthenticationCookies(accessToken, refreshToken, response);

        refreshTokenService.saveCustomerToken(principal.getId(), refreshToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login/staff")
    public ResponseEntity<ApiResponse<Void>> LoginUser(@RequestBody @Validated LoginDTO loginDTO, HttpServletResponse response) {

        Authentication authentication = userAuthenicationProvider.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        if(!authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "Authentication failed", null));
        }

        AccountPrincipal<User> accountPrincipal = (AccountPrincipal<User>) authentication.getPrincipal();

        System.out.println(accountPrincipal.toString());

        TokenDataEntity tokenData = new TokenDataEntity();
        tokenData.setEmail(accountPrincipal.getEmail());
        tokenData.setId(accountPrincipal.getId());
        tokenData.setRole(accountPrincipal.getAuthorities());
        tokenData.setType(TokenDataEntity.Type.STAFF);

        String accessToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.ACCESS);
        String refreshToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.REFRESH); // 30 days

        jwtUtil.setAuthenticationCookies(accessToken, refreshToken, response);

        refreshTokenService.saveUserToken(accountPrincipal.getId(), refreshToken);
        
        return ResponseEntity.noContent().build();
    }

    


}
