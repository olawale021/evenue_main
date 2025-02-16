package com.example.evenue.controller.friends;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.EventService;
import com.example.evenue.service.FriendsService;
import com.example.evenue.models.friends.FriendRequestModel;
import com.example.evenue.service.UserBehaviourService;
import com.example.evenue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/friends")
public class FriendsController {

    @Autowired
    private FriendsService friendsService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserBehaviourService userBehaviourService;


    // View the list of friends for the logged-in user
    @GetMapping
    public String viewFriends(Model model, Authentication authentication) {
        // Get the currently logged-in user
        String userEmail = authentication.getName();
        UserModel currentUser = userService.findUserByEmail(userEmail);

        // Fetch the user's friends
        List<UserModel> friends = friendsService.getFriends(currentUser);

        // Fetch pending friend requests
        List<FriendRequestModel> pendingFriendRequests = friendsService.getPendingFriendRequests(currentUser);

        // Add friends and pending friend requests to the model
        model.addAttribute("friends", friends);
        model.addAttribute("pendingFriendRequests", pendingFriendRequests);

        return "friends"; // Returns a Thymeleaf template named "friends.html"
    }

    // Search for users by username
    @GetMapping("/search")
    public String searchUsers(@RequestParam("username") String username, Authentication authentication, Model model) {
        // Get the currently logged-in user
        String currentUserEmail = authentication.getName();
        UserModel currentUser = userService.findUserByEmail(currentUserEmail);

        // Perform the search and filter out already-friends and the current user
        List<UserModel> searchResults = friendsService.searchUsersToAddAsFriends(currentUser, username);

        // Add search results to the model
        model.addAttribute("searchResults", searchResults);
        return "search-friends";  // This will render a view called search-friends.html
    }

    // Send a friend request
    @PostMapping("/add/{friendId}")
    public String addFriend(@PathVariable Long friendId, Authentication authentication) {
        // Get the currently logged-in user
        String currentUserEmail = authentication.getName();
        UserModel currentUser = userService.findUserByEmail(currentUserEmail);

        // Find the user to be added as a friend
        UserModel friendToAdd = userService.findUserById(Math.toIntExact(friendId));

        // Send friend request
        friendsService.sendFriendRequest(currentUser, friendToAdd);

        // Redirect back to the friends list or another page
        return "redirect:/friends";
    }

    // Accept a friend request
    @PostMapping("/accept/{requestId}")
    public String acceptFriendRequest(@PathVariable Long requestId) {
        friendsService.acceptFriendRequest(requestId);
        return "redirect:/friends";
    }

    // Reject a friend request
    @PostMapping("/reject/{requestId}")
    public String rejectFriendRequest(@PathVariable Long requestId) {
        friendsService.rejectFriendRequest(requestId);
        return "redirect:/friends";
    }

    // View friend details
    @GetMapping("/profile/{friendId}")
    public String viewFriendDetails(@PathVariable Integer friendId, Model model, Authentication authentication) {
        // Find the friend's details
        UserModel friend = userService.findUserById(Math.toIntExact(friendId));

        // Fetch the events the friend is attending based on tickets
        List<EventModel> attendingEvents = eventService.getEventsByUser(friend);

        // Fetch the currently authenticated user
        String userEmail = authentication.getName();
        UserModel loggedInUser = userService.findUserByEmail(userEmail);

        String interactionType = "view_friend_profile";

        // Retrieve the user's location
        String userLocation = loggedInUser.getCity();

        // Log the user's behavior for viewing a friend's profile
        userBehaviourService.logUserBehaviour(
                loggedInUser.getId(), // Current user ID
                null,  // No event ID associated with this action
                interactionType, // Interaction type
                null,  // No event location
                null,  // No date filter
                null,  // No price filter
                null,  // No location filter
                null,  // No session length
                friendId,  // Pass the friend's ID here
                null,  // No ticket quantity
                null,   // No event category ID
                userLocation

        );

        // Log received data for verification
        System.out.println("User Behaviour logged: friend Id: " + friendId + " Interaction Type: " + interactionType);
        // Add data to the model to render in the view
        model.addAttribute("friend", friend);
        model.addAttribute("attendingEvents", attendingEvents);

        return "friend-profile";  // Return Thymeleaf template for friend details
    }


}
