package com.example.evenue.controller.user_behaviour;

import com.example.evenue.models.events.EventCategoryDao;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.EventService;
import com.example.evenue.service.UserBehaviourService;
import com.example.evenue.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/behaviour")
public class UserBehaviourController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventCategoryDao eventCategoryDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBehaviourService userBehaviourService;

    @PostMapping(value = "/save-behaviour", consumes = "text/plain")
    @ResponseBody
    public ResponseEntity<String> saveUserBehaviour(@RequestBody String data, Authentication authentication) {
        // Log to see if the method is triggered
        System.out.println("save behaviour request received with text/plain content type");

        // Parse the plain text data to extract JSON
        Map<String, Object> jsonData;
        try {
            jsonData = new ObjectMapper().readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON data", e);
        }

        // Fetch the logged-in user
        String userEmail = authentication.getName();
        UserModel loggedInUser = userService.findUserByEmail(userEmail);

        // Extract necessary data from the payload
        Long eventId = jsonData.get("eventId") != null ? Long.valueOf(String.valueOf(jsonData.get("eventId"))) : null;
        Long eventCategoryId = jsonData.get("eventCategoryId") != null ? Long.valueOf(String.valueOf(jsonData.get("eventCategoryId"))) : null;
        String eventLocation = jsonData.get("eventLocation") != null ? (String) jsonData.get("eventLocation") : null;
        String interactionType = (String) jsonData.get("interactionType");
        Integer FriendId = jsonData.get("FriendId") != null ? ((Integer) jsonData.get("FriendId")) : null;
        Integer ticketQuantity = jsonData.get("ticketQuantity") != null ? ((Integer) jsonData.get("ticketQuantity")) : null;
        Double timeSpent = ((Number) jsonData.get("timeSpent")).doubleValue();
        Map<String, Object> filterTypeMap = (Map<String, Object>) jsonData.get("filterType");

        // Initialize filter variables
        String dateFilter = null;
        String priceFilter = null;
        String locationFilter = null;

        // Save filter values if present
        if (filterTypeMap != null) {
            dateFilter = (String) filterTypeMap.get("date");
            priceFilter = (String) filterTypeMap.get("price");
            locationFilter = (String) filterTypeMap.get("location");
        }

        // Retrieve the user's location
        String userLocation = loggedInUser.getCity();

//        System.out.println("Event Location from payload: " + eventLocation);
//        // Log received data for verification
//        System.out.println("User Email: " + userEmail);
//        System.out.println("Event ID: " + eventId);
//        System.out.println("Event Location: " + eventLocation);
//        System.out.println("Event Category ID: " + eventCategoryId);
//        System.out.println("Interaction Type: " + interactionType);
//        System.out.println("Time Spent: " + timeSpent + " seconds");

        // Log user behaviour
        userBehaviourService.logUserBehaviour(
                loggedInUser.getId(),
                eventId,
                interactionType,  // Use interaction type from payload
                eventLocation,  // Event location can be added if required
                dateFilter,  // Date filter
                priceFilter,  // Price filter
                locationFilter,  // Location filter
                timeSpent,  // Time spent
                FriendId,  // Friend ID if applicable
                ticketQuantity,  // ticket quantity action if applicable
                eventCategoryId,   // Event category ID if applicable
                userLocation

        );

        // Log received data for verification
        System.out.println("User Behaviour logged: Time Spent: " + timeSpent + " seconds, Interaction Type: " + interactionType);

        return ResponseEntity.ok("Behaviour logged successfully");
    }
}
