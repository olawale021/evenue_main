package com.example.evenue.service;

import com.example.evenue.models.friends.*;
import com.example.evenue.models.users.UserDao;
import com.example.evenue.models.users.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendsService {

    @Autowired
    private FriendsDao friendsDao;

    @Autowired
    private FriendRequestDao friendRequestDao;

    @Autowired
    private UserService userService;

    // Send a friend request
    public FriendRequestModel sendFriendRequest(UserModel sender, UserModel receiver) {
        FriendRequestModel friendRequest = new FriendRequestModel(sender, receiver, FriendRequestStatus.PENDING);
        return friendRequestDao.save(friendRequest);
    }

    // Accept a friend request
    public FriendsModel acceptFriendRequest(Long requestId) {
        Optional<FriendRequestModel> requestOptional = friendRequestDao.findById(requestId);

        if (requestOptional.isPresent()) {
            FriendRequestModel friendRequest = requestOptional.get();
            friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestDao.save(friendRequest);  // Update the request status

            // Add both users to the friends table
            FriendsModel friendship1 = new FriendsModel(friendRequest.getSender(), friendRequest.getReceiver());
            FriendsModel friendship2 = new FriendsModel(friendRequest.getReceiver(), friendRequest.getSender());

            // Save both directions of the friendship
            friendsDao.save(friendship1);
            friendsDao.save(friendship2);

            return friendship1;  // Return one of the friendships for confirmation
        }
        throw new IllegalArgumentException("Friend request not found.");
    }

    // Reject a friend request
    public void rejectFriendRequest(Long requestId) {
        Optional<FriendRequestModel> requestOptional = friendRequestDao.findById(requestId);

        if (requestOptional.isPresent()) {
            FriendRequestModel friendRequest = requestOptional.get();
            friendRequest.setStatus(FriendRequestStatus.REJECTED);
            friendRequestDao.save(friendRequest);
        } else {
            throw new IllegalArgumentException("Friend request not found.");
        }
    }

    // Get all friends of a user
    public List<UserModel> getFriends(UserModel user) {
        List<FriendsModel> friends = friendsDao.findByUser(user);
        return friends.stream()
                .map(FriendsModel::getFriend)
                .collect(Collectors.toList());
    }

    // Search for users by username, excluding current user and existing friends
    public List<UserModel> searchUsersToAddAsFriends(UserModel currentUser, String username) {
        List<UserModel> searchResults = userService.findByUserNameContaining(username);

        // Filter out current user and existing friends
        List<UserModel> currentFriends = getFriends(currentUser);
        return searchResults.stream()
                .filter(user -> !user.equals(currentUser)) // Exclude current user
                .filter(user -> !currentFriends.contains(user)) // Exclude existing friends
                .collect(Collectors.toList());
    }

    // Get friend requests for a user
    public List<FriendRequestModel> getPendingFriendRequests(UserModel user) {
        return friendRequestDao.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);
    }
}
