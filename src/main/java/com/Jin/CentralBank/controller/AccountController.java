package com.Jin.CentralBank.controller;

import com.Jin.CentralBank.model.Account;
import com.Jin.CentralBank.model.User;
import com.Jin.CentralBank.repository.AccountRepository;
import com.Jin.CentralBank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<String> getBalance(Authentication authentication) {
        // Pobranie użytkownika na podstawie tokena
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Nie znaleziono użytkownika.");
        }

        User user = userOpt.get();
        Optional<Account> accountOpt = accountRepository.findByUser(user);

        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Brak konta bankowego.");
        }

        Account account = accountOpt.get();
        BigDecimal balance = account.getBalance();
        return ResponseEntity.ok("💰 Twoje saldo: " + balance + " PLN");
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(Authentication authentication) {
        // Pobranie użytkownika na podstawie tokena
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Nie znaleziono użytkownika.");
        }

        User user = userOpt.get();

        // Sprawdzenie, czy użytkownik ma już konto
        if (accountRepository.findByUser(user).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Błąd: Konto już istnieje.");
        }

        // Tworzenie nowego konta z domyślnym saldem np. 1000 PLN
        Account newAccount = new Account();
        newAccount.setUser(user);
        newAccount.setBalance(BigDecimal.valueOf(1000.00)); // Możesz ustawić inne domyślne saldo

        accountRepository.save(newAccount);

        return ResponseEntity.ok("✅ Konto bankowe utworzone!");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }
}