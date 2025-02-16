package com.example.evenue.controller;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.events.EventDao;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.RecommendationDto;
import com.example.evenue.service.RecommendationService;
import com.example.evenue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private EventDao eventDao;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        // Fetch all events from the database
        List<EventModel> events = eventDao.findAll();

        // Get authenticated user's email from Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Handle popular recommendations based on authentication status
        List<RecommendationDto> popularRecommendations;

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            // Get authenticated user's email
            String userEmail = authentication.getName();

            // Get user ID based on email
            UserModel user = userService.findUserByEmail(userEmail);
            Long userId = Long.valueOf(user.getId());

            // Get personalized recommendations for authenticated user
            popularRecommendations = recommendationService.getPopularRecommendations(userId, 10);
        } else {
            // Get non-personalized recommendations for anonymous users
            popularRecommendations = recommendationService.getPopularRecommendations(null, 10);
        }

        // Add attributes to the model
        model.addAttribute("popularRecommendations", popularRecommendations);
        model.addAttribute("events", events);

        // Add authentication status to model (optional, for UI customization)
        model.addAttribute("isAuthenticated",
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !authentication.getPrincipal().equals("anonymousUser"));

        return "index";
    }
}