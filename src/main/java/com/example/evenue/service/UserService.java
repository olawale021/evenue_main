package com.example.evenue.service;

import com.example.evenue.models.events.EventCategory;
import com.example.evenue.models.events.EventCategoryDao;
import com.example.evenue.models.users.Role;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.models.users.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    private EventCategoryDao eventCategoryDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // Save a new user
    public void saveUser(UserModel user) {
        userDao.save(user);
    }

    // Find user by email
    public UserModel findUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    // Find user by ID
    public UserModel findUserById(Integer id) {
        Optional<UserModel> user = userDao.findById(id);
        return user.orElse(null); // Return user if found, otherwise return null
    }

    // Update user role by ID
    public void updateUserRoleById(Role role, Integer userId) {
        // Fetch the user by ID
        Optional<UserModel> userOpt = userDao.findById(userId);

        if (userOpt.isPresent()) {
            UserModel user = userOpt.get();
            user.setRole(role); // Set the new role
            userDao.save(user); // Save the updated user record
        }
    }

    // Update user role by Email
    public void updateUserRoleByEmail(Role role, String email) {
        UserModel user = findUserByEmail(email);
        if (user != null) {
            user.setRole(role); // Set the new role
            userDao.save(user); // Save the updated user record
        }
    }

    public Optional<UserModel> findUserByUsername(String username) {
        return userDao.findByUserName(username);
    }

    // Optionally, for partial matches
    public List<UserModel> searchUsersByUsername(String username) {
        return userDao.findByUserNameContainingIgnoreCase(username);
    }
    // Method to search for users by a partial username
    public List<UserModel> findByUserNameContaining(String username) {
        return userDao.findByUserNameContainingIgnoreCase(username);
    }

    public void addPreferredCategory(Integer userId, Long categoryId) {
        // Fetch the user
        UserModel user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch the category to add
        EventCategory category = eventCategoryDao.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check if the user already has 3 categories
        if (user.getPreferredCategories().size() >= 3) {
            throw new IllegalArgumentException("Cannot add more than 3 preferred categories");
        }

        // Add the category to the user's preferred categories
        user.getPreferredCategories().add(category);

        // Save the updated user
        userDao.save(user);
    }

    public void updatePreferredCategories(Integer userId, List<Long> selectedCategoryIds) {
        UserModel user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch the selected categories based on IDs
        List<EventCategory> selectedCategories = eventCategoryDao.findAllById(selectedCategoryIds);

        // Limit to 3 categories
        if (selectedCategories.size() > 3) {
            throw new IllegalArgumentException("You can select up to 3 preferred categories.");
        }

        // Update user's preferred categories
        user.setPreferredCategories(new HashSet<>(selectedCategories));
        userDao.save(user);
    }
}
