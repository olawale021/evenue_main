package com.example.evenue.service;

import com.example.evenue.models.events.EventCategory;
import com.example.evenue.models.events.EventCategoryDao;
import com.example.evenue.models.userBehaviour.UserBehaviourDao;
import com.example.evenue.models.userBehaviour.UserBehaviourModel;
import com.example.evenue.models.users.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Service
public class UserBehaviourService {

    @Autowired
    private UserBehaviourDao userBehaviourDao;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventCategoryDao eventCategoryDao;

    public void logUserBehaviour(Integer userId, Long eventId, String interactionType, String eventLocation,
                                 String dateFilter, String priceFilter, String locationFilter, Double sessionLength,
                                 Integer friendId, Integer ticketId, Long eventCategoryId, String userLocation) {

        UserBehaviourModel behaviour = new UserBehaviourModel();
        UserModel user = userService.findUserById(userId);

        behaviour.setUser(user);

        // Set the event if an eventId is provided
        if (eventId != null) {
            eventService.getEventById(eventId).ifPresent(behaviour::setEvent);
        }

        // Set interaction type (e.g., "view" or "browse")
        behaviour.setInteractionType(interactionType);

        // Set the current timestamp for the interaction
        behaviour.setInteractionTimestamp(LocalDateTime.now());

        // Set event category if an eventCategoryId is provided
        if (eventCategoryId != null) {
            Optional<EventCategory> category = eventCategoryDao.findById(eventCategoryId);
            behaviour.setEventCategory(category.orElse(null));  // Set EventCategory if found
        }

        // Set the event location
        behaviour.setEventLocation(eventLocation);

        // Set the new filters (dateFilter, priceFilter, locationFilter)
        behaviour.setDateFilter(dateFilter);       // Set date filter
        behaviour.setPriceFilter(priceFilter);     // Set price filter
        behaviour.setLocationFilter(locationFilter);  // Set location filter

        // Set the session length
        behaviour.setSessionLength(sessionLength);

        // Handle the friendId if it's provided
        if (friendId != null) {
            behaviour.setFriend(userService.findUserById(friendId));
        }

        // Set ticket quantity action (if applicable)
        behaviour.setTicketId(ticketId);

        // Set user location
        behaviour.setUserLocation(userLocation); // Save user location

        // Set the user's preferred categories as individual columns
        Set<EventCategory> preferredCategories = user.getPreferredCategories();
        Iterator<EventCategory> iterator = preferredCategories.iterator();

        if (iterator.hasNext()) {
            behaviour.setPreferredCategory1(iterator.next().getId());
        }
        if (iterator.hasNext()) {
            behaviour.setPreferredCategory2(iterator.next().getId());
        }
        if (iterator.hasNext()) {
            behaviour.setPreferredCategory3(iterator.next().getId());
        }

        // Save to the database
        userBehaviourDao.save(behaviour);
    }
}
