package com.project.MyRh.Controllers.Auth;

import com.project.MyRh.DTO.AuthenticationResponse;
import com.project.MyRh.DTO.Request.AuthenticationRequest;
import com.project.MyRh.DTO.Request.RegisterRequest;
import com.project.MyRh.Services.Auth.LogoutService;
import com.project.MyRh.Services.Auth.ManagerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class ManagerController {

    private final ManagerService service;
    private final LogoutService logout;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        logout.logout(request, response, authentication);
    }

}
