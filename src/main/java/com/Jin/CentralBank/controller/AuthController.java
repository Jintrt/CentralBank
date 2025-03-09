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
        logger.info("🔍 Próba rejestracji użytkownika: {}", user.getUsername());

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Nazwa użytkownika jest wymagana.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Hasło jest wymagane.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Nazwa użytkownika jest już zajęta.");
        }

        // Szyfrujemy hasło przed zapisaniem
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        logger.info("✅ Użytkownik {} został zarejestrowany!", user.getUsername());

        // Automatyczne tworzenie konta bankowego z początkowym saldem 1000 PLN
        Account account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal("1000.00")); // Początkowe saldo
        accountRepository.save(account);
        logger.info("✅ Konto bankowe dla użytkownika {} utworzone.", user.getUsername());

        return ResponseEntity.ok("✅ Rejestracja zakończona sukcesem!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        logger.info("🔍 Próba logowania użytkownika: {}", user.getUsername());

        try {
            Optional<User> dbUserOpt = userRepository.findByUsername(user.getUsername());
            if (dbUserOpt.isEmpty()) {
                logger.warn("❌ Użytkownik {} nie istnieje w bazie!", user.getUsername());
                return ResponseEntity.badRequest().body("❌ Błąd: Niepoprawna nazwa użytkownika lub hasło.");
            }
            User dbUser = dbUserOpt.get();

            // Sprawdzamy, czy hasło jest poprawne
            if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
                logger.warn("❌ Nieprawidłowe hasło dla użytkownika {}!", user.getUsername());
                return ResponseEntity.badRequest().body("❌ Błąd: Niepoprawna nazwa użytkownika lub hasło.");
            }

            // Autoryzacja użytkownika
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            // Generowanie tokena JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("✅ Token JWT wygenerowany dla użytkownika: {}", user.getUsername());

            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            logger.warn("❌ Błędne dane logowania dla użytkownika {}!", user.getUsername());
            return ResponseEntity.badRequest().body("❌ Błąd: Niepoprawna nazwa użytkownika lub hasło.");
        } catch (Exception e) {
            logger.error("❌ Wystąpił błąd logowania: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Błąd serwera. Spróbuj ponownie później.");
        }
    }

    @GetMapping("/secured")
    public ResponseEntity<String> securedEndpoint() {
        return ResponseEntity.ok("🔒 Access granted: Secure endpoint!");
    }
}