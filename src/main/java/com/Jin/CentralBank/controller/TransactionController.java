package com.Jin.CentralBank.controller;

import java.util.List;
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
        System.out.println("🔄 Starting transfer...");

        // Retrieve sender from session
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> senderOpt = userRepository.findByUsername(userDetails.getUsername());
        if (senderOpt.isEmpty()) {
            System.out.println("❌ Error: User not found.");
            return ResponseEntity.badRequest().body("❌ Error: User not found.");
        }
        User sender = senderOpt.get();

        // Retrieve sender's account
        Optional<Account> senderAccountOpt = accountRepository.findByUser(sender);
        if (senderAccountOpt.isEmpty()) {
            System.out.println("❌ Error: No bank account found.");
            return ResponseEntity.badRequest().body("❌ Error: No bank account found.");
        }
        Account senderAccount = senderAccountOpt.get();

        // Retrieve recipient
        Optional<User> receiverOpt = userRepository.findByUsername(transferRequest.getRecipientUsername());
        if (receiverOpt.isEmpty()) {
            System.out.println("❌ Error: Recipient not found.");
            return ResponseEntity.badRequest().body("❌ Error: Recipient not found.");
        }
        User receiver = receiverOpt.get();

        // Retrieve recipient's account
        Optional<Account> receiverAccountOpt = accountRepository.findByUser(receiver);
        if (receiverAccountOpt.isEmpty()) {
            System.out.println("❌ Error: Recipient has no bank account.");
            return ResponseEntity.badRequest().body("❌ Error: Recipient has no bank account.");
        }
        Account receiverAccount = receiverAccountOpt.get();

        // Check if sender and recipient are the same
        if (sender.getUsername().equals(transferRequest.getRecipientUsername())) {
            System.out.println("❌ Error: Cannot send money to yourself.");
            return ResponseEntity.badRequest().body("❌ Error: Cannot send money to yourself.");
        }

        // Check sender's balance
        if (senderAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            System.out.println("❌ Error: Insufficient funds.");
            return ResponseEntity.badRequest().body("❌ Error: Insufficient funds.");
        }

        // Process transaction
        System.out.println("💰 Processing transfer...");
        senderAccount.setBalance(senderAccount.getBalance().subtract(transferRequest.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(transferRequest.getAmount()));

        // Save changes
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // Save transaction
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(transferRequest.getAmount())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        System.out.println("✅ Transfer successful!");
        return ResponseEntity.ok("✅ Transfer successful! " + transferRequest.getAmount() + " PLN to " + transferRequest.getRecipientUsername());
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Error: User not found.");
        }

        User user = userOpt.get();
        List<Transaction> userTransactions = transactionRepository.findBySenderOrReceiver(user, user);

        if (userTransactions.isEmpty()) {
            return ResponseEntity.ok("📜 No transactions found.");
        }

        return ResponseEntity.ok(userTransactions);
    }
}