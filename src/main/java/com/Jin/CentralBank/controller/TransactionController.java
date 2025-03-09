package com.Jin.CentralBank.controller;

import com.Jin.CentralBank.model.Account;
import com.Jin.CentralBank.model.Transaction;
import com.Jin.CentralBank.model.User;
import com.Jin.CentralBank.dto.TransferRequest;
import com.Jin.CentralBank.repository.AccountRepository;
import com.Jin.CentralBank.repository.TransactionRepository;
import com.Jin.CentralBank.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/transfer")
    @Transactional
    public ResponseEntity<String> transferFunds(@RequestBody TransferRequest transferRequest, Authentication authentication) {
        System.out.println("🔄 Rozpoczynam transfer...");

        // Pobieramy użytkownika (nadawcę) z sesji
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> senderOpt = userRepository.findByUsername(userDetails.getUsername());
        if (senderOpt.isEmpty()) {
            System.out.println("❌ Błąd: Nie znaleziono użytkownika.");
            return ResponseEntity.badRequest().body("❌ Błąd: Nie znaleziono użytkownika.");
        }
        User sender = senderOpt.get();

        // Pobieramy konto nadawcy
        Optional<Account> senderAccountOpt = accountRepository.findByUser(sender);
        if (senderAccountOpt.isEmpty()) {
            System.out.println("❌ Błąd: Brak konta bankowego.");
            return ResponseEntity.badRequest().body("❌ Błąd: Brak konta bankowego.");
        }
        Account senderAccount = senderAccountOpt.get();

        // Pobieramy odbiorcę
        Optional<User> receiverOpt = userRepository.findByUsername(transferRequest.getRecipientUsername());
        if (receiverOpt.isEmpty()) {
            System.out.println("❌ Błąd: Nie znaleziono odbiorcy.");
            return ResponseEntity.badRequest().body("❌ Błąd: Nie znaleziono odbiorcy.");
        }
        User receiver = receiverOpt.get();

        // Pobieramy konto odbiorcy
        Optional<Account> receiverAccountOpt = accountRepository.findByUser(receiver);
        if (receiverAccountOpt.isEmpty()) {
            System.out.println("❌ Błąd: Odbiorca nie ma konta.");
            return ResponseEntity.badRequest().body("❌ Błąd: Odbiorca nie ma konta.");
        }
        Account receiverAccount = receiverAccountOpt.get();

        // Sprawdzenie, czy nadawca i odbiorca to ta sama osoba
        if (sender.getUsername().equals(transferRequest.getRecipientUsername())) {
            System.out.println("❌ Błąd: Nie można wysłać przelewu do samego siebie.");
            return ResponseEntity.badRequest().body("❌ Błąd: Nie można wysłać przelewu do samego siebie.");
        }

        // Sprawdzamy saldo nadawcy
        if (senderAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            System.out.println("❌ Błąd: Niewystarczające saldo.");
            return ResponseEntity.badRequest().body("❌ Błąd: Niewystarczające saldo.");
        }

        // Realizacja transakcji
        System.out.println("💰 Realizacja przelewu...");
        senderAccount.setBalance(senderAccount.getBalance().subtract(transferRequest.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(transferRequest.getAmount()));

        // Zapisujemy zmiany
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // Zapisujemy transakcję
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(transferRequest.getAmount())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        System.out.println("✅ Przelew wykonany pomyślnie!");
        return ResponseEntity.ok("✅ Przelew wykonany pomyślnie! " + transferRequest.getAmount() + " PLN do " + transferRequest.getRecipientUsername());
    }
}