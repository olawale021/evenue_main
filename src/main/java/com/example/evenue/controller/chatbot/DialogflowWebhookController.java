package com.example.evenue.controller.chatbot;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketModel;
import com.example.evenue.models.tickets.TicketTypeModel;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.EventService;
import com.example.evenue.service.TicketService;
import com.example.evenue.service.TicketTypeService;
import com.example.evenue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhook")
public class DialogflowWebhookController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private TicketService ticketService;

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@RequestBody Map<String, Object> request) {
        System.out.println("Received: " + request);
        Map<String, Object> response = new HashMap<>();
        response.put("fulfillmentText", "Test successful!");
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> handleDialogflowWebhook(@RequestBody Map<String, Object> request) {
        // ✅ Check if queryResult exists
        if (request == null || !request.containsKey("queryResult")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("fulfillmentText", "Invalid request: Missing 'queryResult'");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> queryResult = (Map<String, Object>) request.get("queryResult");
        Map<String, Object> intent = (Map<String, Object>) queryResult.get("intent");

        // ✅ Check if intent exists
        if (intent == null || !intent.containsKey("displayName")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("fulfillmentText", "Invalid request: Missing 'intent'");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String intentName = (String) intent.get("displayName");

        // Handle the intent based on its name
        switch (intentName) {
            case "CollectEmailIntent":
                return handleCollectEmailIntent(queryResult, request);
            case "CollectEventIntent":
                return handleCollectEventIntent(queryResult, request);
            case "CollectTicketTypeIntent":
                return handleCollectTicketTypeIntent(queryResult, request);
            case "ConfirmBookingIntent":
                return handleConfirmBookingIntent(queryResult, request);
            default:
                return handleFallbackIntent();
        }
    }

    private ResponseEntity<Map<String, Object>> handleCollectEmailIntent(Map<String, Object> queryResult, Map<String, Object> request) {
        // Extract and normalize email from query parameters
        Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");
        String email = (String) parameters.get("email");

        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "Please provide a valid email address.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Normalize email: Trim spaces and convert to lowercase
        email = email.trim().toLowerCase();

        // Check if the email exists in the database
        UserModel user = userService.findUserByEmail(email);
        if (user == null) {
            // If user not found, prompt to enter a registered email or sign up
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "This email is not registered. Enter a registered email or type 'yes' to create an account with the provided email.");

            // Set context to redirect user to either re-enter email or initiate sign-up
            fulfillmentResponse.put("outputContexts", List.of(
                    Map.of(
                            "name", request.get("session") + "/contexts/awaiting_email_or_signup",
                            "lifespanCount", 5,
                            "parameters", Map.of("email", email) // Store email for potential sign-up
                    )
            ));

            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Save user_id and email in the session
        Map<String, Object> sessionParameters = new HashMap<>();
        sessionParameters.put("user_id", user.getId());
        sessionParameters.put("email", email);

        // Respond with event selection prompt and session context
        Map<String, Object> fulfillmentResponse = new HashMap<>();
        fulfillmentResponse.put("fulfillmentText", "Which event would you like to book tickets for?");
        fulfillmentResponse.put("outputContexts", List.of(
                Map.of(
                        "name", request.get("session") + "/contexts/awaiting_event_name",
                        "lifespanCount", 5,
                        "parameters", sessionParameters
                )
        ));

        return ResponseEntity.ok(fulfillmentResponse);
    }


    @Autowired
    private TicketTypeService ticketTypeService;

    private ResponseEntity<Map<String, Object>> handleCollectEventIntent(Map<String, Object> queryResult, Map<String, Object> request) {
        // Extract event name and get event_id from the database
        Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");
        String eventName = ((String) parameters.get("event")).trim();

        // Replace non-breaking spaces with regular spaces
        eventName = eventName.replace("\u00A0", " ");

        // Print the event name after normalization
        System.out.println("Normalized event name: " + eventName);

        // Find event by name (case-insensitive search)
        Optional<EventModel> eventOpt = eventService.findByEventName(eventName);

        if (eventOpt.isEmpty()) {
            // Event not found, prompt the user to provide a valid event name
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "I'm sorry, I couldn't find the event: " + eventName + ". Please provide a valid event name.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        EventModel event = eventOpt.get();

        // Retrieve available ticket types for the event
        List<TicketTypeModel> ticketTypes = ticketTypeService.getTicketTypesByEventId(event.getId());

        if (ticketTypes.isEmpty()) {
            // No ticket types available for this event
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "I'm sorry, there are no tickets available for this event.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Build a response showing ticket type options
        StringBuilder ticketOptions = new StringBuilder("Here are the available ticket types for " + eventName + ":\n");
        for (TicketTypeModel ticketType : ticketTypes) {
            ticketOptions.append(ticketType.getTypeName()).append(" - $").append(ticketType.getPrice()).append("\n");
        }
        ticketOptions.append("Please choose a ticket type.");

        // ✅ Safely check for outputContexts
        List<Map<String, Object>> outputContexts = (List<Map<String, Object>>) queryResult.get("outputContexts");
        Map<String, Object> sessionParameters = new HashMap<>();

        if (outputContexts != null) {
            sessionParameters = outputContexts.stream()
                    .filter(context -> ((String) context.get("name")).endsWith("/contexts/awaiting_event_name"))
                    .findFirst()
                    .map(context -> (Map<String, Object>) context.get("parameters"))
                    .orElse(new HashMap<>());
        }

        sessionParameters.put("event_id", event.getId());
        sessionParameters.put("event_name", eventName);

        // Respond with ticket type options and update session context
        Map<String, Object> fulfillmentResponse = new HashMap<>();
        fulfillmentResponse.put("fulfillmentText", ticketOptions.toString());

        if (outputContexts != null) {
            fulfillmentResponse.put("outputContexts", List.of(
                    Map.of(
                            "name", request.get("session") + "/contexts/awaiting_ticket_type",
                            "lifespanCount", 10,
                            "parameters", sessionParameters
                    )
            ));
        }

        return ResponseEntity.ok(fulfillmentResponse);
    }

    private ResponseEntity<Map<String, Object>> handleCollectTicketTypeIntent(Map<String, Object> queryResult, Map<String, Object> request) {
        // Extract ticket type and quantity from user input
        Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");

        // Safely extract ticket type and handle null case
        String ticketTypeName = (String) parameters.get("ticketType");
        if (ticketTypeName == null || ticketTypeName.isEmpty()) {
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "Please specify a ticket type.");
            return ResponseEntity.ok(fulfillmentResponse);
        }
        ticketTypeName = ticketTypeName.trim(); // Safe to trim after null check

        // Safely extract quantity and handle null case
        Object quantityObj = parameters.get("quantity");
        Integer quantity = null;
        if (quantityObj instanceof Double) {
            quantity = ((Double) quantityObj).intValue();  // Convert Double to Integer
        } else if (quantityObj instanceof Integer) {
            quantity = (Integer) quantityObj;
        }

        if (quantity == null || quantity <= 0) {
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "Please specify a valid number of tickets.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Retrieve previous session parameters (event_id, event_name, and email)
        List<Map<String, Object>> outputContexts = (List<Map<String, Object>>) queryResult.get("outputContexts");
        Map<String, Object> sessionParameters = outputContexts.stream()
                .filter(context -> ((String) context.get("name")).endsWith("/contexts/awaiting_ticket_type"))
                .findFirst()
                .map(context -> new HashMap<>((Map<String, Object>) context.get("parameters"))) // Copy to a new map
                .orElse(new HashMap<>());

        Object eventIdObj = sessionParameters.get("event_id");
        Long eventId = null;
        if (eventIdObj instanceof Double) {
            eventId = ((Double) eventIdObj).longValue();  // Convert Double to Long
        } else if (eventIdObj instanceof Long) {
            eventId = (Long) eventIdObj;
        }

        // Get event name from sessionParameters or from EventModel
        String eventName = (String) sessionParameters.get("event_name");

        if (eventId == null || eventName == null) {
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "Event ID or event name is missing. Please try again.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Check if the email is available, otherwise ask the user to provide it
        String userEmail = (String) sessionParameters.get("email");
        if (userEmail == null || userEmail.isEmpty()) {
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "Please provide your email to assign the ticket.");
            fulfillmentResponse.put("outputContexts", List.of(
                    Map.of(
                            "name", request.get("session") + "/contexts/awaiting_email",
                            "lifespanCount", 5,
                            "parameters", sessionParameters // Now using the copy of sessionParameters
                    )
            ));
            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Retrieve the ticket types again for verification
        List<TicketTypeModel> ticketTypes = ticketTypeService.getTicketTypesByEventId(eventId);

        // Verify the selected ticket type is available for the event
        String finalTicketTypeName = ticketTypeName;
        TicketTypeModel selectedTicketType = ticketTypes.stream()
                .filter(ticketType -> ticketType.getTypeName().name().equalsIgnoreCase(finalTicketTypeName))  // Use enum's name() method
                .findFirst()
                .orElse(null);

        if (selectedTicketType == null) {
            // Ticket type not found, prompt the user again
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "I'm sorry, I couldn't find the ticket type: " + ticketTypeName + ". Please choose a valid ticket type.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        // Save the selected ticket type, quantity, and email in the session
        sessionParameters.put("ticket_type_id", selectedTicketType.getTicketTypeId());
        sessionParameters.put("ticket_type_name", selectedTicketType.getTypeName().name());  // Store the enum's name
        sessionParameters.put("ticket_price", selectedTicketType.getPrice());
        sessionParameters.put("quantity", quantity);

        // Respond with a confirmation message including the event name and email
        String confirmationMessage = "You selected " + quantity + " " + selectedTicketType.getTypeName().name() +
                " tickets for the event '" + eventName + "', priced at $" + selectedTicketType.getPrice() + " each. " +
                "The tickets will be assigned to " + userEmail + ".";
        Map<String, Object> fulfillmentResponse = new HashMap<>();
        fulfillmentResponse.put("fulfillmentText", confirmationMessage + " Would you like to confirm the booking?");
        fulfillmentResponse.put("outputContexts", List.of(
                Map.of(
                        "name", request.get("session") + "/contexts/awaiting_confirmation",
                        "lifespanCount", 10,
                        "parameters", sessionParameters // Now using the copy of sessionParameters
                )
        ));

        return ResponseEntity.ok(fulfillmentResponse);
    }


    private ResponseEntity<Map<String, Object>> handleConfirmBookingIntent(Map<String, Object> queryResult, Map<String, Object> request) {
        // Retrieve session parameters (event_id, ticket_type, quantity, etc.)
        List<Map<String, Object>> outputContexts = (List<Map<String, Object>>) queryResult.get("outputContexts");
        Map<String, Object> sessionParameters = outputContexts.stream()
                .filter(context -> ((String) context.get("name")).endsWith("/contexts/awaiting_confirmation"))
                .findFirst()
                .map(context -> (Map<String, Object>) context.get("parameters"))
                .orElse(new HashMap<>());

        // Handle eventId safely
        Object eventIdObj = sessionParameters.get("event_id");
        Long eventId = null;
        if (eventIdObj instanceof Double) {
            eventId = ((Double) eventIdObj).longValue();  // Convert Double to Long
        } else if (eventIdObj instanceof Long) {
            eventId = (Long) eventIdObj;
        }

        // Handle ticketTypeId safely
        Object ticketTypeIdObj = sessionParameters.get("ticket_type_id");
        Long ticketTypeId = null;
        if (ticketTypeIdObj instanceof Double) {
            ticketTypeId = ((Double) ticketTypeIdObj).longValue();  // Convert Double to Long
        } else if (ticketTypeIdObj instanceof Long) {
            ticketTypeId = (Long) ticketTypeIdObj;
        }

        // Handle quantity safely
        Object quantityObj = sessionParameters.get("quantity");
        Integer quantity = null;
        if (quantityObj instanceof Double) {
            quantity = ((Double) quantityObj).intValue();  // Convert Double to Integer
        } else if (quantityObj instanceof Integer) {
            quantity = (Integer) quantityObj;
        }

        String userEmail = (String) sessionParameters.get("email");

        // Validate essential data
        if (eventId == null || ticketTypeId == null || quantity == null || userEmail == null) {
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "Unable to process your booking. Some information is missing.");
            return ResponseEntity.ok(fulfillmentResponse);
        }

        try {
            // Fetch the user by email
            UserModel user = userService.findUserByEmail(userEmail);
            if (user == null) {
                Map<String, Object> fulfillmentResponse = new HashMap<>();
                fulfillmentResponse.put("fulfillmentText", "User not found. Please provide a valid email.");
                return ResponseEntity.ok(fulfillmentResponse);
            }

            // Fetch the event and ticket type
            Optional<EventModel> eventOpt = eventService.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                Map<String, Object> fulfillmentResponse = new HashMap<>();
                fulfillmentResponse.put("fulfillmentText", "Event not found. Please provide a valid event.");
                return ResponseEntity.ok(fulfillmentResponse);
            }
            EventModel event = eventOpt.get();

            TicketTypeModel ticketType = ticketTypeService.getTicketTypeById(ticketTypeId);
            if (ticketType == null) {
                Map<String, Object> fulfillmentResponse = new HashMap<>();
                fulfillmentResponse.put("fulfillmentText", "Ticket type not found. Please provide a valid ticket type.");
                return ResponseEntity.ok(fulfillmentResponse);
            }

            // Create the ticket booking entry in the database
            TicketModel ticket = new TicketModel();
            ticket.setUser(user);
            ticket.setEvent(event);
            ticket.setTicketType(ticketType);
            ticket.setQuantity(quantity);
            ticket.setPrice(ticketType.getPrice() * quantity);  // Calculate total price
            ticket.setPurchaseDate(LocalDateTime.now());

            // Set createdAt and updatedAt timestamps
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            // Generate a unique ticket code
            ticket.generateTicketCode();

            // Save the ticket booking
            ticketService.saveTicket(ticket);

            // Respond with a confirmation message, including the ticket code
            String confirmationMessage = "Your booking for " + quantity + " " + ticketType.getTypeName().name() +
                    " tickets to the event '" + event.getEventName() + "' has been confirmed. Your ticket code is: " + ticket.getTicketCode() +
                    ". Total cost: $" + ticket.getPrice() + ".";
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", confirmationMessage);
            return ResponseEntity.ok(fulfillmentResponse);

        } catch (Exception e) {
            // Log the error message and stack trace for troubleshooting
            System.out.println("Error during booking: " + e.getMessage());
            e.printStackTrace();

            // Handle any errors during the booking process
            Map<String, Object> fulfillmentResponse = new HashMap<>();
            fulfillmentResponse.put("fulfillmentText", "An error occurred while processing your booking. Please try again.");
            return ResponseEntity.ok(fulfillmentResponse);
        }
    }




    private ResponseEntity<Map<String, Object>> handleFallbackIntent() {
        // Handle fallback case where the intent is unrecognized
        Map<String, Object> fulfillmentResponse = new HashMap<>();
        fulfillmentResponse.put("fulfillmentText", "I'm sorry, I didn't understand that. Could you please rephrase?");
        return ResponseEntity.ok(fulfillmentResponse);
    }
}


