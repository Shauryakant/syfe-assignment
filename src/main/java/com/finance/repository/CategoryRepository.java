package com.finance.repository;

import com.finance.entity.Category;
import com.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsCustomFalse();

    List<Category> findByUser(User user);

    @Query("SELECT c FROM Category c WHERE c.isCustom = false OR c.user = :user")
    List<Category> findAllForUser(@Param("user") User user);

    @Query("SELECT c FROM Category c WHERE (c.isCustom = false OR c.user = :user) AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByNameForUser(@Param("name") String name, @Param("user") User user);

    Optional<Category> findByNameAndUser(String name, User user);

    Optional<Category> findByNameAndIsCustomFalse(String name);

    boolean existsByNameAndUser(String name, User user);

    boolean existsByNameAndIsCustomFalse(String name);
}
