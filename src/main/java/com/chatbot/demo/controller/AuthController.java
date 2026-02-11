package com.chatbot.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.demo.entity.User;
import com.chatbot.demo.repository.UserRepository;
import com.chatbot.demo.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepo,
            PasswordEncoder encoder,
            JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    // =====================
    // SIGNUP
    // =====================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {

        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Account already exists");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);

        // ✅ Generate token using correct service
        String token = jwtService.generateToken(savedUser.getId());

        // ✅ Return token
        return ResponseEntity.ok(token);
    }

    // =====================
    // LOGIN
    // =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        User dbUser = userRepo.findByEmail(user.getEmail())
                .orElse(null);

        if (dbUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        String token = jwtService.generateToken(dbUser.getId());
        return ResponseEntity.ok(token);
    }
}
