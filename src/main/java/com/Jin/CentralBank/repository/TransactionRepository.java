package com.Jin.CentralBank.repository;

import com.Jin.CentralBank.model.Transaction;
import com.Jin.CentralBank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Retrieve transactions for sender or receiver (sorted from newest)
    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);

    // Retrieve all transactions for a user (as sender or receiver)
    List<Transaction> findBySenderOrReceiver(User user, User user2);
}