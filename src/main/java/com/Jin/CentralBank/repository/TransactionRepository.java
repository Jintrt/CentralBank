package com.Jin.CentralBank.repository;

import com.Jin.CentralBank.model.Transaction;
import com.Jin.CentralBank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Pobieranie transakcji dla nadawcy lub odbiorcy (posortowane od najnowszych)
    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);

    // Pobieranie wszystkich transakcji dla użytkownika (nadawca lub odbiorca)
    List<Transaction> findBySenderOrReceiver(User user, User user2);
}