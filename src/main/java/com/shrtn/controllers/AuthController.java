package com.shrtn.controllers;

import com.shrtn.dto.GoogleLoginRequest;
import com.shrtn.dto.LoginRequest;
import com.shrtn.dto.RegisterRequest;
import com.shrtn.models.User;
import com.shrtn.services.GoogleAuthService;
import com.shrtn.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {


    private UserService userService;
    private GoogleAuthService googleAuthService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){

        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setRole("ROLE_USER");
            user.setPassword(registerRequest.getPassword());
            userService.registerUser(user);
            return ResponseEntity.ok("User registered Successfully !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            Map<String, Object> googleClaims = googleAuthService.verifyToken(googleLoginRequest.getIdToken());
            String email = (String) googleClaims.get("email");
            String name = (String) googleClaims.get("name");
            return ResponseEntity.ok(userService.loginUserWithGoogle(email, name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

