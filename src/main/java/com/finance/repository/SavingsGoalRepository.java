package com.finance.repository;

import com.finance.entity.SavingsGoal;
import com.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUser(User user);

    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
