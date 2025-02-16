package com.example.evenue.models.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageDao extends JpaRepository<PostImageModel, Long> {
    List<PostImageModel> findByPostId(Long postId);

    // Finds the image by its URL and associated post
    Optional<PostImageModel> findByImageUrlAndPost(String imageUrl, PostModel post);
}

