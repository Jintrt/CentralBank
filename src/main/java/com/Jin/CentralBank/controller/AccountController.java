package com.Jin.CentralBank.controller;

import com.Jin.CentralBank.model.Account;
import com.Jin.CentralBank.model.User;
import com.Jin.CentralBank.repository.AccountRepository;
import com.Jin.CentralBank.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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
        // Retrieve the user based on the token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: User not found.");
        }

        User user = userOpt.get();
        Optional<Account> accountOpt = accountRepository.findByUser(user);

        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: No bank account found.");
        }

        Account account = accountOpt.get();
        BigDecimal balance = account.getBalance();
        return ResponseEntity.ok("💰 Your balance: " + balance + " PLN");
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(Authentication authentication) {
        // Retrieve the user based on the token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: User not found.");
        }

        User user = userOpt.get();

        // Check if the user already has an account
        if (accountRepository.findByUser(user).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Error: Account already exists.");
        }

        // Create a new account with a default balance, e.g., 1000 PLN
        Account newAccount = new Account();
        newAccount.setUser(user);
        newAccount.setBalance(BigDecimal.valueOf(1000.00)); // You can set a different default balance

        accountRepository.save(newAccount);

        return ResponseEntity.ok("✅ Bank account created!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @GetMapping("/details")
    public ResponseEntity<?> getAccountDetails(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: User not found.");
        }

        User user = userOpt.get();
        Optional<Account> accountOpt = accountRepository.findByUser(user);

        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: No bank account found.");
        }

        Account account = accountOpt.get();
        return ResponseEntity.ok(account);
    }
}