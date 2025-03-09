package com.Jin.CentralBank.repository;

import com.Jin.CentralBank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}