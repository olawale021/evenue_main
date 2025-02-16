package com.example.evenue.controller.user;

import com.example.evenue.models.events.EventCategory;
import com.example.evenue.models.events.EventCategoryDao;
import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketModel;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.EventService;
import com.example.evenue.service.TicketService;
import com.example.evenue.service.UserService;
import com.example.evenue.models.users.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@SessionAttributes("userId") // Using Spring's session management
public class UserController {

    private final UserService userService; // Inject UserService
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private TicketService ticketService;


    @Autowired
    private EventService eventService;

    @Autowired
    EventCategoryDao eventCategoryDao;


    // Serve the registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register"; // Return register.html
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login"; // Return login.html
    }

    @PostMapping("/logout")
    public String logout(Model model) {
        model.addAttribute("userId", null); // Clear the user ID from the model
        SecurityContextHolder.clearContext(); // Clear the security context
        return "redirect:/users/login"; // Redirect to the login page
    }

    // Register a new user with email and password, and set user ID in session
    @PostMapping("/register")
    public String registerUser(@RequestParam String email, @RequestParam String password, Model model) {
        if (userService.findUserByEmail(email) != null) { // Use userService
            model.addAttribute("error", "Email already in use.");
            return "register"; // Return to the registration form with an error message
        }

        UserModel newUser = new UserModel();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // Encrypt the password
        newUser.setRole(null); // Initially, the role is set to null

        userService.saveUser(newUser); // Save the new user using userService
        model.addAttribute("userId", newUser.getId()); // Store user ID in the model
        return "redirect:/users/set-role"; // Redirect to the role selection page
    }


    // Handle user login
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model) {
        UserModel user = userService.findUserByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid email or password.");
            return "login"; // Return to the login form with an error message
        }

        // If role is null, redirect to set-role page
        if (user.getRole() == null) {
            model.addAttribute("userId", user.getId());
            return "redirect:/users/set-role";
        }

        model.addAttribute("userId", user.getId());
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), password, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Redirect based on role
        if (user.getRole() == Role.ORGANIZER) {
            return "redirect:/organizer/dashboard";
        } else if (user.getRole() == Role.ATTENDEE) {
            return "redirect:/users/dashboard";
        } else {
            return "redirect:/users/set-role"; // Fallback in case role is not set correctly
        }
    }



    // Serve the dashboard page
    @GetMapping("/attendee/dashboard")
    public String showDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        String email = authentication.getName(); // This will be the email address
        UserModel user = userService.findUserByEmail(email); // Use userService

        if (user == null) {
            // This shouldn't happen if the user is authenticated, but just in case
            return "redirect:/users/login";
        }

        // Create a Pageable object with page number and size
        Pageable pageable = PageRequest.of(page, size);
        // Fetch all events for the dropdown (without pagination)
        List<EventModel> allEventsForDropdown = eventService.getAllEventsForDropdown();

        // Fetch the paginated list of events
        Page<EventModel> events = eventService.getAllEvents(pageable);

        // Add all events (non-paginated) for the dropdown to the model
        model.addAttribute("allEvents", allEventsForDropdown);
        model.addAttribute("user", user);
        model.addAttribute("events", events.getContent());  // Get the event content for this page
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", events.getTotalPages());

        return "dashboard";
    }


    // Serve the role selection page
    @GetMapping("/set-role")
    public String showRoleSelectionForm(Authentication authentication, Model model) {
        // Retrieve the email of the authenticated user
        String userEmail = authentication.getName();

        // Fetch user by email
        UserModel user = userService.findUserByEmail(userEmail);
        if (user == null) {
            return "redirect:/users/register"; // Redirect if the user does not exist
        }


        model.addAttribute("userId", user.getId());
        model.addAttribute("email", user.getEmail());

        return "set-role"; // Return the role selection page
    }



    // Handle role selection
    @PostMapping("/set-role")
    public String setUserRole(@RequestParam String role, Model model, Authentication authentication) {
        // Retrieve the email of the authenticated user
        String userEmail = authentication.getName();

        // Fetch user by email
        UserModel user = userService.findUserByEmail(userEmail); // Ensure you have this method
        if (user == null) {
            return "redirect:/users/register"; // Redirect if the user does not exist
        }

        // Convert the string role to the Role enum
        try {
            Role userRole = Role.valueOf(role.toUpperCase());

            // Update the role in the database
            userService.updateUserRoleById(userRole, user.getId());

            // Update the security context with the new role
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
            Authentication newAuth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), authorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // Determine redirect based on new role
            if (userRole == Role.ORGANIZER) {
                return "redirect:/organizer/dashboard";
            } else {
                return "redirect:/users/preferred-categories"; // Use user ID from the user object
            }
        } catch (IllegalArgumentException e) {
            // Invalid role provided
            return "redirect:/users/set-role?error=invalid_role";
        }
    }


    // Serve the edit profile page
    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        String email = authentication.getName(); // Get the current user's email from authentication
        UserModel user = userService.findUserByEmail(email);

        if (user == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", user);
        return "edit-profile"; // Serve edit-profile.html template
    }
    // Handle profile updates
    @PostMapping("/profile/edit")
    public String updateProfile(
            @RequestParam String userName,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String addressLine1,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String postalCode,
            @RequestParam String country,
            @RequestParam String contactNumber,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        String currentEmail = authentication.getName(); // Current logged in user's email
        UserModel user = userService.findUserByEmail(currentEmail);
        if (user == null) {
            return "redirect:/users/login";
        }

        // Update the user's profile fields
        user.setUserName(userName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setAddressLine1(addressLine1);  // Set individual address fields
        user.setCity(city);
        user.setState(state);
        user.setPostalCode(postalCode);
        user.setCountry(country);
        user.setContactNumber(contactNumber);

        userService.saveUser(user); // Save the updated user using the userService

        // Update session and security context if the email was changed
        if (!currentEmail.equals(email)) {
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            Authentication newAuth = new UsernamePasswordAuthenticationToken(user.getEmail(), authentication.getCredentials(), authorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        model.addAttribute("user", user);
        return "redirect:/users/profile/edit"; // Redirect to the profile page
    }


    // Serve the profile page
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        String email = authentication.getName(); // Current user's email
        UserModel user = userService.findUserByEmail(email);
        model.addAttribute("user", user);

        return "profile"; // Return profile.html template
    }

    // Serve the logged-in user's tickets page
    @GetMapping("/tickets")
    public String getUserTickets(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login"; // If the user is not authenticated, redirect to login
        }

        String email = authentication.getName(); // Fetch the current user's email
        UserModel user = userService.findUserByEmail(email);
        if (user == null) {
            return "redirect:/users/login"; // If user not found, redirect to login
        }

        // Fetch the user's tickets using the user ID
        List<TicketModel> userTickets = ticketService.getTicketsByUserId(user.getId());
        model.addAttribute("tickets", userTickets); // Add tickets to the model

        return "user-tickets"; // Return the Thymeleaf template for displaying user tickets
    }

    // Serve the upcoming events for which the logged-in user has purchased tickets
    @GetMapping("/upcoming-events")
    public String getUserUpcomingEvents(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login"; // If the user is not authenticated, redirect to login
        }

        String email = authentication.getName(); // Fetch the current user's email
        UserModel user = userService.findUserByEmail(email);
        if (user == null) {
            return "redirect:/users/login"; // Redirect to login if user not found
        }

        // Fetch the user's tickets
        List<TicketModel> userTickets = ticketService.getTicketsByUserId(user.getId());

        // Extract the events from the tickets and filter for upcoming events
        LocalDateTime now = LocalDateTime.now();
        List<EventModel> upcomingEvents = userTickets.stream()
                .map(TicketModel::getEvent) // Get the event associated with each ticket
                .filter(event -> event.getEventDate().isAfter(ChronoLocalDate.from(now))) // Filter only future events
                .distinct() // Ensure the same event is not added multiple times
                .collect(Collectors.toList());

        model.addAttribute("events", upcomingEvents); // Add events to the model
        return "user-upcoming-events"; // Return the Thymeleaf template for displaying upcoming events
    }

    // Method to render the form with categories
    @GetMapping("/preferred-categories")
    public String showPreferredCategoriesForm(Model model) {
        // Fetch the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Use email to fetch the user
        UserModel user = userService.findUserByEmail(email);  // Assuming you have a findByEmail method

        // Fetch all event categories
        List<EventCategory> categories = eventCategoryDao.findAll();

        // Add user and categories to the model
        model.addAttribute("user", user);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategories", user.getPreferredCategories());

        return "preferred-categories-form";  // Thymeleaf template name
    }

    // Method to handle form submission
    @PostMapping("/{userId}/preferred-categories")
    public String updateUserPreferences(@PathVariable Integer userId, @RequestParam String city, @RequestParam List<Long> selectedCategories, Model model) {
        // Update user's preferred categories and city in the database
        UserModel user = userService.findUserById(userId);
        user.setCity(city);
        user.setPreferredCategories(selectedCategories.stream()
                .map(categoryId -> eventCategoryDao.findById(categoryId).orElse(null))
                .collect(Collectors.toSet()));
        userService.saveUser(user);

        // Redirect or return to a success page
        return "redirect:/users/attendee/dashboard"; // Change to your desired redirect
    }
}
