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
import com.yield.barbershop_backend.service.UserService;
import com.yield.barbershop_backend.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




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
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/customer/login")
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

    @PostMapping("/staff/login")
    public ResponseEntity<ApiResponse<Void>> LoginUser(@RequestBody @Validated LoginDTO loginDTO, HttpServletResponse response) {

        Authentication authentication = userAuthenicationProvider.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        if(!authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "Authentication failed", null));
        }

        AccountPrincipal<User> accountPrincipal = (AccountPrincipal<User>) authentication.getPrincipal();

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

    @GetMapping("/staff/verify-email")
    public ResponseEntity<Void> verifyEmailStaff(@RequestParam String token, @RequestParam Long userId) {
        userService.verifyUserEmail(userId, token);
       return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/staff/request-email-verification")
    public ResponseEntity<Void> requestStaffEmailVerification() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AccountPrincipal<User> accountPrincipal = (AccountPrincipal<User>) authentication.getPrincipal();

        Long accountId = accountPrincipal.getId();

        userService.sendUserEmailVerification(accountId);

        return ResponseEntity.noContent().build();
    }
    
        


}
