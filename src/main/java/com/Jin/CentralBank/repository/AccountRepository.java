package com.Jin.CentralBank.repository;

import com.Jin.CentralBank.model.Account;
import com.Jin.CentralBank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Find account by user
    Optional<Account> findByUser(User user);

    // Find account by user ID
    Optional<Account> findByUserId(Long userId);
}