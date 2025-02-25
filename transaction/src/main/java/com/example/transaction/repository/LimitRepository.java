package com.example.transaction.repository;

import com.example.transaction.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LimitRepository extends JpaRepository<Limit, Integer> {
    Optional<Limit> findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(String account, String limitCategory);
    Optional<Limit> findTopByAccountAndLimitCategory(String account, String limitCategory);
}

