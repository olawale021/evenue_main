package com.example.evenue.models.friends;

import com.example.evenue.models.users.UserModel;
import jakarta.persistence.*;

@Entity
@Table(name = "friends")
public class FriendsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private UserModel friend;

    // Default constructor required by JPA
    public FriendsModel() {}

    // Parameterized constructor
    public FriendsModel(UserModel user, UserModel friend) {
        this.user = user;
        this.friend = friend;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public UserModel getFriend() {
        return friend;
    }

    public void setFriend(UserModel friend) {
        this.friend = friend;
    }

    @Override
    public String toString() {
        return "FriendsModel{" +
                "id=" + id +
                ", user=" + user.getUserName() +
                ", friend=" + friend.getUserName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendsModel that = (FriendsModel) o;

        if (!user.equals(that.user)) return false;
        return friend.equals(that.friend);
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + friend.hashCode();
        return result;
    }
}
