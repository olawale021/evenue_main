package com.example.evenue.models.users;

import com.example.evenue.models.users.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<UserModel, Integer> {

    // Custom query to update the user role by user ID
    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.role = :role WHERE u.id = :id")
    void updateUserRoleById(@Param("role") Role role, @Param("id") int id);

    // Custom query to update the user role by email
    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.role = :role WHERE u.email = :email")
    void updateUserRoleByEmail(String role, String email);

    // Other custom query methods
    UserModel findByEmail(String email);

    // Find user by username
    Optional<UserModel> findByUserName(String username);

    // Optionally, for partial matches
    List<UserModel> findByUserNameContainingIgnoreCase(String username);
}