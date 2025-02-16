package com.example.evenue.models.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostDao extends JpaRepository<PostModel, Long> {
    List<PostModel> findAllByUserId(Long userId);
    List<PostModel> findAllByEventId(Long eventId);
    List<PostModel> findByUserId(Long userId);
    List<PostModel> findAllByOrderByCreatedAtDesc();
}
