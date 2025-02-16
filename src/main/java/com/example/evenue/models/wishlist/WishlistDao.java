package com.example.evenue.models.wishlist;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.users.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistDao extends JpaRepository<WishlistModel, Long> {
    List<WishlistModel> findByUser(UserModel user);
    List<WishlistModel> findByEvent(EventModel event);

    // Find a wishlist item by user and event
    Optional<WishlistModel> findByUserAndEvent(UserModel user, EventModel event);
}
