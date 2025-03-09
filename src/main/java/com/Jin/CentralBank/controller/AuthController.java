package com.Jin.CentralBank.controller;

import com.Jin.CentralBank.model.Account;
import com.Jin.CentralBank.model.User;
import com.Jin.CentralBank.repository.AccountRepository;
import com.Jin.CentralBank.repository.UserRepository;
import com.Jin.CentralBank.security.JwtUtil;
import com.Jin.CentralBank.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, AccountRepository accountRepository, JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        logger.info("🔍 Attempting to register user: {}", user.getUsername());

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: Username is required.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: Password is required.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Error: Username is already taken.");
        }

        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        logger.info("✅ User {} has been registered!", user.getUsername());

        // Automatically create a bank account with an initial balance of 1000 PLN
        Account account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal("1000.00")); // Initial balance
        accountRepository.save(account);
        logger.info("✅ Bank account for user {} created.", user.getUsername());

        return ResponseEntity.ok("✅ Registration successful!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        logger.info("🔍 Attempting to log in user: {}", user.getUsername());

        try {
            Optional<User> dbUserOpt = userRepository.findByUsername(user.getUsername());
            if (dbUserOpt.isEmpty()) {
                logger.warn("❌ User {} does not exist in the database!", user.getUsername());
                return ResponseEntity.badRequest().body("❌ Error: Invalid username or password.");
            }
            User dbUser = dbUserOpt.get();

            // Check if the password is correct
            if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
                logger.warn("❌ Incorrect password for user {}!", user.getUsername());
                return ResponseEntity.badRequest().body("❌ Error: Invalid username or password.");
            }

            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("✅ JWT token generated for user: {}", user.getUsername());

            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            logger.warn("❌ Incorrect login credentials for user {}!", user.getUsername());
            return ResponseEntity.badRequest().body("❌ Error: Invalid username or password.");
        } catch (Exception e) {
            logger.error("❌ Login error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Server error. Please try again later.");
        }
    }

    @GetMapping("/secured")
    public ResponseEntity<String> securedEndpoint() {
        return ResponseEntity.ok("🔒 Access granted: Secure endpoint!");
    }
}