package com.example.evenue.models.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventCategoryDao extends JpaRepository<EventCategory, Long> {

    // Find all categories preferred by a specific user
    @Query("SELECT c FROM EventCategory c JOIN c.users u WHERE u.id = :userId")
    List<EventCategory> findCategoriesByUserId(@Param("userId") Integer userId);

}