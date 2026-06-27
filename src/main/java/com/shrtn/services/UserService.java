package com.shrtn.services;

import com.shrtn.dto.JwtAuthenticationResponse;
import com.shrtn.dto.LoginRequest;
import com.shrtn.models.User;
import com.shrtn.repository.UserRepository;
import com.shrtn.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    public User registerUser(User user){

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public JwtAuthenticationResponse loginUser(LoginRequest loginRequest){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse loginUserWithGoogle(String email, String name) {
        User user;
        if (userRepository.existsByEmail(email)) {
            user = userRepository.findByEmail(email).get();
        } else {
            // Create a new user (signup)
            user = new User();
            user.setEmail(email);
            // generate a unique username
            String baseUsername = name != null && !name.trim().isEmpty() ? name.replaceAll("\\s+", "").toLowerCase() : email.split("@")[0];
            String username = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter;
                counter++;
            }
            user.setUsername(username);
            user.setRole("ROLE_USER");
            // Set a random secure password for OAuth user
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            user = userRepository.save(user);
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    public User findByUsername(String name){
        return userRepository.findByUsername(name).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: "+name)
        );
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: "+email)
        );
    }
}

