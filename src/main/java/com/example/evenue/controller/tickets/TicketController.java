package com.example.evenue.controller.tickets;


import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketModel;
import com.example.evenue.models.tickets.TicketTypeModel;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserBehaviourService userBehaviourService;

    // Endpoint to show the form for adding a new ticket type to an event
    @GetMapping("/types/add/{eventId}")
    public String showAddTicketTypeForm(@PathVariable Long eventId, Model model) {
        Optional<EventModel> eventOpt = eventService.getEventById(eventId);
        if (eventOpt.isEmpty()) {
            model.addAttribute("error", "Event not found");
            return "error";
        }

        EventModel event = eventOpt.get();
        TicketTypeModel ticketType = new TicketTypeModel();
        ticketType.setEvent(event); // Set the event object to the ticket type

        model.addAttribute("event", event);
        model.addAttribute("ticketType", new TicketTypeModel());
        return "add-ticket-type"; // The form template to add ticket type
    }


    // Endpoint to handle the creation of a new ticket type
    @PostMapping("/types/add")
    public String addTicketType(
            @ModelAttribute("ticketType") TicketTypeModel ticketType,
            @RequestParam("eventId") Long eventId,
            Model model) {

        Optional<EventModel> eventOpt = eventService.getEventById(eventId);
        if (eventOpt.isEmpty()) {
            model.addAttribute("error", "Event not found");
            return "error";
        }

        EventModel event = eventOpt.get();
        ticketType.setEvent(event);

        // Validate ticket type details
        if (ticketType.getPrice() == null || ticketType.getQuantity() == null || ticketType.getTypeName() == null) {
            model.addAttribute("error", "All fields are required.");
            model.addAttribute("event", event);
            return "add-ticket-type";
        }

        ticketType.setRemainingQuantity(ticketType.getQuantity());

        try {
            ticketTypeService.saveTicketType(ticketType);
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while saving the ticket type. Please try again.");
            model.addAttribute("event", event);
            return "add-ticket-type";
        }

        return "redirect:/tickets/types/" + eventId;
    }




    // Endpoint to view all ticket types for an event
    @GetMapping("/types/{eventId}")
    public String viewTicketTypes(@PathVariable Long eventId, Model model) {
        Optional<EventModel> eventOpt = eventService.getEventById(eventId);
        if (eventOpt.isEmpty()) {
            model.addAttribute("error", "Event not found");
            return "error";
        }

        EventModel event = eventOpt.get();
        List<TicketTypeModel> ticketTypes = ticketTypeService.getTicketTypesByEventId(eventId);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypes);
        return "view-ticket-types";
    }


    // Endpoint to show form for creating a ticket for a specific ticket type
    @GetMapping("/buy/{ticketTypeId}")
    public String showCreateTicketForm(@PathVariable Long ticketTypeId,
                                       @RequestParam("quantity") Integer quantity,
                                       Model model) {
        TicketTypeModel ticketType = ticketTypeService.getTicketTypeById(ticketTypeId);
        if (ticketType == null) {
            model.addAttribute("error", "Invalid ticket type.");
            return "error";
        }

        if (ticketType.getRemainingQuantity() <= 0) {
            model.addAttribute("error", "This ticket type is sold out.");
            return "error";
        }

        if (quantity <= 0 || quantity > ticketType.getRemainingQuantity()) {
            model.addAttribute("error", "Please select a valid quantity.");
            return "redirect:/events/details/" + ticketType.getEvent().getId();
        }

        model.addAttribute("ticketType", ticketType);
        model.addAttribute("quantity", quantity);
        return "confirm-ticket-purchase";
    }


    @PostMapping("/buy")
    public String createTicket(
            @RequestParam("ticketTypeId") Long ticketTypeId,
            @RequestParam("eventId") Long eventId,
            @RequestParam("quantity") Integer quantity,
            Model model) {

        // Debugging log
        System.out.println("Ticket Type ID: " + ticketTypeId);
        System.out.println("Event ID: " + eventId);
        System.out.println("Quantity: " + quantity);

        // Retrieve the currently authenticated user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UserModel user = userService.findUserByEmail(userEmail);



        if (user == null) {
            model.addAttribute("error", "User not authenticated.");
            return "confirm-ticket-purchase";
        }

        // Retrieve event and ticket type
        Optional<EventModel> eventOpt = eventService.getEventById(eventId);
        Optional<TicketTypeModel> ticketTypeOpt = Optional.ofNullable(ticketTypeService.getTicketTypeById(ticketTypeId));

        if (eventOpt.isEmpty() || ticketTypeOpt.isEmpty()) {
            model.addAttribute("error", "Invalid event or ticket type.");
            return "confirm-ticket-purchase";
        }

        EventModel event = eventOpt.get();
        TicketTypeModel ticketType = ticketTypeOpt.get();

        if (ticketType.getRemainingQuantity() < quantity) {
            model.addAttribute("error", "Not enough tickets available for this type.");
            return "confirm-ticket-purchase";
        }

        // Create and populate the ticket
        TicketModel ticket = new TicketModel();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setTicketType(ticketType);
        ticket.setQuantity(quantity);
        ticket.setPrice(ticketType.getPrice() * quantity);
        ticket.setTicketCode(generateTicketCode());
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // Save the ticket
        ticketService.saveTicket(ticket);

        // Update remaining quantity in ticket type
        ticketType.setRemainingQuantity(ticketType.getRemainingQuantity() - quantity);
        ticketTypeService.saveTicketType(ticketType);

        // Retrieve the user's location
        String userLocation = user.getCity();



        String interactionType = "purchase";
        // Log user purchase activity
        userBehaviourService.logUserBehaviour(
                user.getId(),
                eventId,
                interactionType,  // Set interaction type as 'purchase'
                event.getLocation(),
                null,  // No date filter
                null,  // No price filter
                null,  // No location filter
                null,  // Time spent can be calculated if needed
                null,  // No friend ID
                ticket.getId(),  // Ticket quantity
                ticketType.getEvent().getEventCategory() != null ? ticketType.getEvent().getEventCategory().getId() : null,  // Pass the event category ID
                userLocation
        );

        // Log received data for verification
        System.out.println("User Behaviour logged: ticket quantity: " + quantity + " Interaction Type: " + interactionType);

        // Redirect to success page and pass the ticket code
        return "redirect:/tickets/success?ticketCode=" + ticket.getTicketCode();
    }




    @GetMapping("/success")
    public String showPurchaseSuccess(@RequestParam("ticketCode") String ticketCode, Model model) {
        model.addAttribute("ticketCode", ticketCode);
        return "purchase-success";
    }



    // Helper method to generate a unique ticket code
    private String generateTicketCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * Endpoint to verify a ticket based on its ticket code.
     *
     * @param //ticketCode The unique code of the ticket to be verified.
     * @param model The model to pass data to the view.
     * @return The verification result page.
     */
    @GetMapping("/verify")
    public String showVerifyTicketForm(Model model) {
        // Add an empty attribute to bind with the form
        model.addAttribute("ticketCode", "");
        return "ticket-verification-form";  // Return the form view
    }

    @PostMapping("/verify")
    public String verifyTicket(@RequestParam("ticketCode") String ticketCode, Model model) {
        // Retrieve the ticket by the ticketCode
        TicketModel ticket = ticketService.getTicketByTicketCode(ticketCode);

        if (ticket == null) {
            model.addAttribute("error", "Ticket not found or invalid ticket code.");
            return "ticket-verification-error";  // Error page if the ticket is invalid
        }

        // Check if the ticket has already been scanned
        if (ticket.getIsScanned()) {
            model.addAttribute("error", "This ticket has already been scanned.");
            return "ticket-verification-error";  // Error page if the ticket is already scanned
        }

        // Mark the ticket as scanned and update the scanned time
        ticket.setIsScanned(true);
        ticket.setScannedAt(LocalDateTime.now());
        ticketService.saveTicket(ticket);

        // Retrieve the user email and event details
        UserModel user = ticket.getUser();
        EventModel event = ticket.getEvent();

        // Pass the ticket and additional details to the model for the success page
        model.addAttribute("ticket", ticket);
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("eventName", event.getEventName());
        return "ticket-verification-success";  // Success page for valid ticket
    }


}
