package com.example.evenue.models.friends;

import com.example.evenue.models.users.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestDao extends JpaRepository<FriendRequestModel, Long> {

    // Find friend requests by receiver and status
    List<FriendRequestModel> findByReceiverAndStatus(UserModel receiver, FriendRequestStatus status);

    // Find friend requests by sender and status
    List<FriendRequestModel> findBySenderAndStatus(UserModel sender, FriendRequestStatus status);

    // Find friend requests between two users (either sender or receiver)
    List<FriendRequestModel> findBySenderAndReceiver(UserModel sender, UserModel receiver);

    // Optional additional methods depending on use case
    List<FriendRequestModel> findBySender(UserModel sender);

    List<FriendRequestModel> findByReceiver(UserModel receiver);
}
