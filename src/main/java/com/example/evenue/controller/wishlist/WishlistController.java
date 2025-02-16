package com.example.evenue.controller.wishlist;

import com.example.evenue.models.events.EventDao;
import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.models.wishlist.WishlistModel;
import com.example.evenue.service.UserBehaviourService;
import com.example.evenue.service.WishlistService;
import com.example.evenue.service.UserService;
import com.example.evenue.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private UserBehaviourService userBehaviourService;

    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);


    @GetMapping
    public String getWishlistByUser(Model model) {
        // Retrieve the currently authenticated user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Fetch the user by email (assuming you have a method to find by email)
        Optional<UserModel> user = Optional.ofNullable(userService.findUserByEmail(userEmail));

        if (user.isPresent()) {
            // Fetch wishlist items associated with the user
            List<WishlistModel> wishlistItems = wishlistService.getWishlistByUser(user.get());

            // Log wishlist items for debugging purposes
            logger.info("Logging wishlist items for user: " + userEmail);
            for (WishlistModel item : wishlistItems) {
                logger.info("Wishlist ID: " + item.getId() + ", Event Name: " + item.getEvent().getEventName() + ", Event ID: " + item.getEvent().getId());
            }

            // Add the list of wishlist items to the model, for rendering in the view
            model.addAttribute("wishlistItems", wishlistItems);

            // Fetch all events from the database
            List<EventModel> events = eventDao.findAll();

            // Add the events list to the model to be used in the view
            model.addAttribute("events", events);

            // Return the name of the Thymeleaf template for the wishlist view
            return "wishlist";
        } else {
            // Handle the case when the user is not found (shouldn't happen if authenticated)
            model.addAttribute("errorMessage", "User not found.");
            return "error"; // Redirect to an error page if the user is not found
        }
    }




    // Add an event to the wishlist
    @PostMapping("/add")
    public String addToWishlist(@RequestParam Long userId, @RequestParam Long eventId, Model model) {
        Optional<UserModel> user = Optional.ofNullable(userService.findUserById(Math.toIntExact(userId)));
        Optional<EventModel> event = eventService.getEventById(eventId);

        if (user.isPresent() && event.isPresent()) {
            wishlistService.addEventToWishlist(user.get(), event.get());
            return "redirect:/wishlist" + userId; // Redirect to the user's wishlist
        } else {
            model.addAttribute("errorMessage", "User or Event not found.");
            return "error"; // Redirect to error page
        }
    }

    @DeleteMapping("/remove/{wishlistId}")
    public String removeFromWishlist(@PathVariable Long wishlistId, Model model) {
        boolean removed = wishlistService.removeFromWishlist(wishlistId);
        if (removed) {
            return "redirect:/wishlist"; // Redirect back to the wishlist after removing the item
        } else {
            model.addAttribute("errorMessage", "Wishlist item not found.");
            return "error"; // Redirect to error page
        }
    }


    // Check if an event is liked by a specific user
    @GetMapping("/isLikedAndInWishlist")
    public @ResponseBody boolean isLikedAndInWishlist(@RequestParam Integer userId, @RequestParam Long eventId) {
        Optional<UserModel> user = Optional.ofNullable(userService.findUserById(userId));
        Optional<EventModel> event = eventService.getEventById(eventId);

        if (user.isPresent() && event.isPresent()) {
            return wishlistService.isEventLikedByUser(user.get(), event.get());
        } else {
            throw new RuntimeException("User or Event not found."); // Throw exception if user or event not found
        }
    }

    // Toggle the like (add or remove) and redirect
    @PostMapping("/toggleLike")
    public String toggleLike(@RequestParam Long userId, @RequestParam Long eventId, Model model) {
        Optional<UserModel> user = Optional.ofNullable(userService.findUserById(Math.toIntExact(userId)));
        Optional<EventModel> event = eventService.getEventById(eventId);

        // Retrieve the currently authenticated user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UserModel loggedInUser = userService.findUserByEmail(userEmail);

        if (user.isPresent() && event.isPresent()) {
            boolean wasLiked = wishlistService.isEventLikedByUser(user.get(), event.get()); // Check if the event is already liked
            wishlistService.toggleLike(user.get(), event.get()); // Toggle the like status

            // Retrieve the user's location
            String userLocation = loggedInUser.getCity();

            // Track user behavior
            String interactionType = wasLiked ? "remove_from_wishlist" : "add_to_wishlist"; // Determine interaction type
            userBehaviourService.logUserBehaviour(
                    user.get().getId(),
                    eventId,
                    interactionType,  // Set interaction type
                    event.get().getLocation(),  // Set event location
                    null,  // No date filter
                    null,  // No price filter
                    null,  // No location filter
                    null,  // Time spent can be calculated if needed
                    null,  // No friend ID
                    null,  // No ticket quantity
                    event.get().getEventCategory() != null ? event.get().getEventCategory().getId() : null, // Pass the event category ID
                    userLocation
            );
            // Log received data for verification
            System.out.println("User Behaviour logged: event Id: " + eventId + " Interaction Type: " + interactionType);

            return "redirect:/events/browse"; // Redirect to the event browsing page
        } else {
            model.addAttribute("errorMessage", "User or Event not found.");
            return "error"; // Redirect to error page
        }
    }

}
