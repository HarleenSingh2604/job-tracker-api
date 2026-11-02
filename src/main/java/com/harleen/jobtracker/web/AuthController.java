package com.harleen.jobtracker.web;

import com.harleen.jobtracker.dto.Dtos.Credentials;
import com.harleen.jobtracker.dto.Dtos.TokenResponse;
import com.harleen.jobtracker.model.AppUser;
import com.harleen.jobtracker.repo.UserRepository;
import com.harleen.jobtracker.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody Credentials body) {
        if (users.existsByUsername(body.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        users.save(new AppUser(body.username(), encoder.encode(body.password())));
        return new TokenResponse(jwt.generate(body.username()));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody Credentials body) {
        AppUser user = users.findByUsername(body.username())
                .filter(u -> encoder.matches(body.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return new TokenResponse(jwt.generate(user.getUsername()));
    }
}
