package com.example.evenue.controller.chatbot;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketTypeModel;
import com.example.evenue.models.tickets.TicketTypeName;
import com.example.evenue.models.users.UserModel;
import com.example.evenue.service.EventService;
import com.example.evenue.service.TicketTypeService;
import com.example.evenue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private TicketTypeService ticketTypeService;

    // Inner classes representing the structure of Dialogflow request and response
    public static class DialogflowRequest {
        private QueryResult queryResult;
        private String session;

        public QueryResult getQueryResult() {
            return queryResult;
        }

        public void setQueryResult(QueryResult queryResult) {
            this.queryResult = queryResult;
        }

        public String getSession() {
            return session;
        }

        public void setSession(String session) {
            this.session = session;
        }
    }

    public static class QueryResult {
        private Intent intent;
        private Map<String, String> parameters;
        private OutputContext[] outputContexts;

        public Intent getIntent() {
            return intent;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        public OutputContext[] getOutputContexts() {
            return outputContexts;
        }

        public void setOutputContexts(OutputContext[] outputContexts) {
            this.outputContexts = outputContexts;
        }
    }

    public static class Intent {
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    public static class OutputContext {
        private String name;
        private Map<String, Object> parameters;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getParameters() {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    public static class DialogflowResponse {
        private String fulfillmentText;
        private OutputContext[] outputContexts;

        public DialogflowResponse(String fulfillmentText) {
            this.fulfillmentText = fulfillmentText;
        }

        public String getFulfillmentText() {
            return fulfillmentText;
        }

        public void setFulfillmentText(String fulfillmentText) {
            this.fulfillmentText = fulfillmentText;
        }

        public OutputContext[] getOutputContexts() {
            return outputContexts;
        }

        public void setOutputContexts(OutputContext[] outputContexts) {
            this.outputContexts = outputContexts;
        }
    }

    // Main fulfillment handler
    @PostMapping("/fulfillment")
    public ResponseEntity<DialogflowResponse> handleFulfillment(@RequestBody DialogflowRequest request) {
        String intentName = request.getQueryResult().getIntent().getDisplayName();
        String sessionId = request.getSession().split("/sessions/")[1]; // Extract session ID

        System.out.println("Received request with session: " + sessionId);
        System.out.println("Intent Name: " + intentName);
        System.out.println("Parameters: " + request.getQueryResult().getParameters());

        switch (intentName) {
            case "request.email":
                return handleEmailRequest(request.getQueryResult().getParameters(), sessionId);

            case "request.eventName":
                return handleEventRequest(request.getQueryResult().getParameters(), request.getQueryResult().getOutputContexts(), sessionId);

            case "request.ticketType":
                return handleTicketTypeRequest(request.getQueryResult().getParameters(), request.getQueryResult().getOutputContexts(), sessionId);

            default:
                return new ResponseEntity<>(new DialogflowResponse("Sorry, I didn't understand that request."), HttpStatus.OK);
        }
    }

    // Handle the email intent and store user information in session context
    private ResponseEntity<DialogflowResponse> handleEmailRequest(Map<String, String> parameters, String sessionId) {
        String userEmail = parameters.get("userEmail");

        // Log parameters
        System.out.println("handleEmailRequest: userEmail=" + userEmail);

        UserModel user = userService.findUserByEmail(userEmail);

        if (user != null) {
            // Log found user
            System.out.println("Found user with email: " + userEmail);

            // Construct the correct context name
            String contextPath = String.format("projects/%s/agent/sessions/%s/contexts/user_details", "steppa-osnw", sessionId);

            // Create new context to store user data
            OutputContext emailContext = new OutputContext();
            emailContext.setName(contextPath);
            emailContext.getParameters().put("user_id", user.getId());
            emailContext.getParameters().put("userEmail", userEmail);
            emailContext.getParameters().put("lifespanCount", 5); // Increase lifespan

            DialogflowResponse response = new DialogflowResponse("Great! Let's proceed with the booking. What's the name of the event?");
            response.setOutputContexts(new OutputContext[]{emailContext});

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            System.out.println("User not found for email: " + userEmail);
            return new ResponseEntity<>(new DialogflowResponse("I couldn't find an account with that email. Please sign up or provide a registered email address."), HttpStatus.OK);
        }
    }

    // Handle the event request and store event information in session context
    private ResponseEntity<DialogflowResponse> handleEventRequest(Map<String, String> parameters, OutputContext[] contexts, String sessionId) {
        String eventName = parameters.get("eventName");

        // Log parameters and contexts
        System.out.println("handleEventRequest: eventName=" + eventName);
        System.out.println("Contexts: " + Arrays.toString(contexts));

        // Retrieve user details from session context
        Map<String, Object> sessionData = getContextParameters(contexts, "user_details");
        System.out.println("Session data: " + sessionData);

        if (sessionData == null || sessionData.get("user_id") == null) {
            System.out.println("User session data is missing.");
            return new ResponseEntity<>(new DialogflowResponse("User session is not valid. Please start again."), HttpStatus.OK);
        }

        // Use Optional for event lookup
        Optional<EventModel> eventOpt = eventService.findByEventName(eventName);
        if (eventOpt.isPresent()) {
            EventModel event = eventOpt.get();

            // Set up context path and output context for event details
            String contextPath = String.format("projects/%s/agent/sessions/%s/contexts/event_details", "steppa-osnw", sessionId);
            OutputContext eventContext = new OutputContext();
            eventContext.setName(contextPath);
            eventContext.getParameters().put("event_id", event.getId());

            // Build and return a DialogflowResponse
            DialogflowResponse response = new DialogflowResponse("Great! What type of ticket would you like to buy (e.g., VIP, General)?");
            response.setOutputContexts(new OutputContext[]{eventContext});
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // Handle case where event is not found
            return new ResponseEntity<>(new DialogflowResponse("Sorry, I couldn't find an event with that name. Please provide a valid event name."), HttpStatus.OK);
        }
    }


    // Handle the ticket type request and calculate total price based on quantity
    private ResponseEntity<DialogflowResponse> handleTicketTypeRequest(Map<String, String> parameters, OutputContext[] contexts, String sessionId) {
        String ticketTypeStr = parameters.get("ticketType");
        String quantityStr = parameters.get("quantity");
        System.out.println("handleTicketTypeRequest: ticketType=" + ticketTypeStr + ", quantity=" + quantityStr);

        Integer quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(new DialogflowResponse("Please provide a valid quantity."), HttpStatus.OK);
        }

        TicketTypeName ticketType;
        try {
            ticketType = TicketTypeName.valueOf(ticketTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new DialogflowResponse("Invalid ticket type. Please provide a valid ticket type."), HttpStatus.OK);
        }

        Map<String, Object> sessionData = getContextParameters(contexts, "event_details");
        if (sessionData == null || sessionData.get("event_id") == null) {
            return new ResponseEntity<>(new DialogflowResponse("Event session is not valid. Please start again."), HttpStatus.OK);
        }

        Long eventId = (Long) sessionData.get("event_id");
        TicketTypeModel ticket = ticketTypeService.findTicketTypeByEventIdAndType(eventId, ticketType);

        if (ticket != null) {
            Double totalPrice = ticket.getPrice() * quantity;
            DialogflowResponse response = new DialogflowResponse("You chose " + quantity + " " + ticketType + " tickets. Total price: $" + totalPrice + ". Proceed to payment?");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new DialogflowResponse("Sorry, I couldn't find that ticket type for the event. Please provide a valid ticket type."), HttpStatus.OK);
        }
    }

    // Helper method to get parameters from a given context
    private Map<String, Object> getContextParameters(OutputContext[] contexts, String contextName) {
        for (OutputContext context : contexts) {
            System.out.println("Available context: " + context.getName()); // Log all contexts
            if (context.getName().contains(contextName)) {
                System.out.println("Context found: " + contextName + " with parameters: " + context.getParameters());
                return context.getParameters();
            }
        }
        System.out.println("Context not found: " + contextName);
        return null;
    }

}
