package com.example.evenue.controller.recommendation;


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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showRecommendations(Model model) {
        // Fetch authenticated user's email from Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // Typically the email or username

        // Retrieve user ID based on email
        UserModel user = userService.findUserByEmail(userEmail);
        Long userId = Long.valueOf(user.getId());

        int n = 10; // Number of recommendations

        List<RecommendationDto> hybridRecommendations = recommendationService.getHybridRecommendations(userId, n);
        List<RecommendationDto> popularRecommendations = recommendationService.getPopularRecommendations(userId, n);
        List<RecommendationDto> categoryRecommendations = recommendationService.getCategoryRecommendations(userId, n);
        List<RecommendationDto> profileRecommendations = recommendationService.getProfileRecommendations(userId, n);
        List<RecommendationDto> friendsRecommendations = recommendationService.getFriendsRecommendations(userId, n);

        model.addAttribute("hybridRecommendations", hybridRecommendations);
        model.addAttribute("popularRecommendations", popularRecommendations);
        model.addAttribute("categoryRecommendations", categoryRecommendations);
        model.addAttribute("profileRecommendations", profileRecommendations);
        model.addAttribute("friendsRecommendations", friendsRecommendations);

        return "recommendations";
    }
}
