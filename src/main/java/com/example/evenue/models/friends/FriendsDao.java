package com.example.evenue.models.friends;

import com.example.evenue.models.users.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsDao extends JpaRepository<FriendsModel, Long> {

    // Retrieve the list of friends for a specific user
    List<FriendsModel> findByUser(UserModel user);

    // Retrieve the list of users who have a specific user as a friend
    List<FriendsModel> findByFriend(UserModel user);

    // Check if a user and a friend are already friends
    Optional<FriendsModel> findByUserAndFriend(UserModel user, UserModel friend);
}
