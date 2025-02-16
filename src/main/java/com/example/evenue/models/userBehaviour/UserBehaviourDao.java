package com.example.evenue.models.userBehaviour;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBehaviourDao extends JpaRepository<UserBehaviourModel, Long> {
    // Add custom queries if needed
}
