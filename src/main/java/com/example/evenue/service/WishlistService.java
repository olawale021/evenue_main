package com.example.evenue.service;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.models.wishlist.WishlistDao;
import com.example.evenue.models.wishlist.WishlistModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistDao wishlistDao;

    // Get all wishlist events for a user
    public List<WishlistModel> getWishlistByUser(UserModel user) {
        return wishlistDao.findByUser(user);
    }


    // Add an event to the user's wishlist
    public WishlistModel addEventToWishlist(UserModel user, EventModel event) {
        Optional<WishlistModel> wishlistItem = wishlistDao.findByUserAndEvent(user, event);
        if (wishlistItem.isEmpty()) {
            WishlistModel newWishlistItem = new WishlistModel(user, event);
            return wishlistDao.save(newWishlistItem);
        } else {
            return wishlistItem.get(); // Already exists
        }
    }

    // Remove an event from the user's wishlist by wishlist ID
    public boolean removeFromWishlist(Long wishlistId) {
        Optional<WishlistModel> wishlistItem = wishlistDao.findById(wishlistId);
        if (wishlistItem.isPresent()) {
            wishlistDao.delete(wishlistItem.get());
            return true;
        }
        return false;
    }

    // Check if an event is liked by the user
    public boolean isEventLikedByUser(UserModel user, EventModel event) {
        return wishlistDao.findByUserAndEvent(user, event).isPresent();
    }

    // Toggle like (add or remove) for an event
    public void toggleLike(UserModel user, EventModel event) {
        Optional<WishlistModel> wishlistItem = wishlistDao.findByUserAndEvent(user, event);
        if (wishlistItem.isPresent()) {
            wishlistDao.delete(wishlistItem.get()); // Remove from wishlist if exists
        } else {
            WishlistModel newWishlistItem = new WishlistModel(user, event);
            wishlistDao.save(newWishlistItem); // Add to wishlist
        }
    }
}
