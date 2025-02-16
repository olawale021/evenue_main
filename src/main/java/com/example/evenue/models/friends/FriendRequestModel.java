package com.example.evenue.models.friends;

import com.example.evenue.models.users.UserModel;
import jakarta.persistence.*;

@Entity
@Table(name = "friend_requests")
public class FriendRequestModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserModel sender;  // The user who sends the friend request

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserModel receiver;  // The user who receives the friend request

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status;  // Pending, Accepted, Rejected

    // Constructors
    public FriendRequestModel() {}

    public FriendRequestModel(UserModel sender, UserModel receiver, FriendRequestStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getSender() {
        return sender;
    }

    public void setSender(UserModel sender) {
        this.sender = sender;
    }

    public UserModel getReceiver() {
        return receiver;
    }

    public void setReceiver(UserModel receiver) {
        this.receiver = receiver;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }
}
