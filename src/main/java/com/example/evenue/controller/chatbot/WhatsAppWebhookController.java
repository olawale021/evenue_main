package com.example.evenue.controller.chatbot;

import com.example.evenue.utils.AccessTokenGenerator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.UUID;


@RestController
@RequestMapping("/whatsapp-webhook")
public class WhatsAppWebhookController {

    private static final String VERIFY_TOKEN = "";
    private static final String WHATSAPP_API_URL = "";
    private static final String ACCESS_TOKEN = "";
    private static final Logger LOGGER = Logger.getLogger(WhatsAppWebhookController.class.getName());
    private static final String DIALOGFLOW_URL_TEMPLATE = "";
//    private static final String DIALOGFLOW_ACCESS_TOKEN = "";

    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String mode,
                                                @RequestParam("hub.verify_token") String token,
                                                @RequestParam("hub.challenge") String challenge) {
        LOGGER.info("Webhook verification request received: mode=" + mode + ", token=" + token);
        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            LOGGER.info("Webhook verification successful.");
            return ResponseEntity.ok(challenge);
        } else {
            LOGGER.warning("Webhook verification failed.");
            return ResponseEntity.status(403).body("Verification failed");
        }
    }

    @PostMapping
    public ResponseEntity<String> handleIncomingMessage(@RequestBody Map<String, Object> request) {
        LOGGER.info("Incoming payload: " + request);

        try {
            Map<String, Object> entry = ((List<Map<String, Object>>) request.get("entry")).get(0);
            Map<String, Object> changes = ((List<Map<String, Object>>) entry.get("changes")).get(0);
            Map<String, Object> value = (Map<String, Object>) changes.get("value");

            // ✅ Ensure we are processing a user message, not just status updates
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            if (messages == null || messages.isEmpty()) {
                LOGGER.warning("No messages found in the payload.");
                return ResponseEntity.status(400).body("No messages found");
            }

            Map<String, Object> message = messages.get(0);
            String from = (String) message.get("from"); // Sender's phone number
            String userMessage = (String) ((Map<String, Object>) message.get("text")).get("body");

            // ✅ Normalize event name formatting (same as DialogflowWebhookController)
            userMessage = userMessage.trim().replace("\u00A0", " "); // Replace non-breaking spaces

            LOGGER.info("Received message from " + from + ": " + userMessage);

            // Generate a dynamic session ID based on the sender's phone number
            String sessionId = UUID.nameUUIDFromBytes(from.getBytes()).toString();

            // ✅ Send the correctly formatted text to Dialogflow
            String dialogflowResponse = sendToDialogflow(sessionId, userMessage);

            // Send the Dialogflow response back to the user via WhatsApp
            sendWhatsAppResponse(from, dialogflowResponse);

            return ResponseEntity.ok("Message processed successfully");
        } catch (Exception e) {
            LOGGER.severe("Error processing incoming message: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing message");
        }
    }


    private String sendToDialogflow(String sessionId, String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        // ✅ Normalize userMessage format (same as DialogflowWebhookController)
        userMessage = userMessage.trim().replace("\u00A0", " ");

        Map<String, Object> queryInput = Map.of(
                "text", Map.of(
                        "text", userMessage,
                        "languageCode", "en"
                )
        );

        Map<String, Object> dialogflowPayload = Map.of(
                "queryInput", queryInput
        );

        String dialogflowUrl = String.format(DIALOGFLOW_URL_TEMPLATE, sessionId);

        try {
            String accessToken = AccessTokenGenerator.getAccessToken().trim();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(dialogflowPayload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(dialogflowUrl, requestEntity, Map.class);

            LOGGER.info("Dialogflow API Response: " + response.getBody());

            // ✅ Extract fulfillment text properly
            Map<String, Object> queryResult = (Map<String, Object>) response.getBody().get("queryResult");
            return (String) queryResult.get("fulfillmentText");
        } catch (Exception e) {
            LOGGER.severe("Error communicating with Dialogflow: " + e.getMessage());
            return "Sorry, I couldn't process your request.";
        }
    }



    private void sendWhatsAppResponse(String recipient, String messageText) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", recipient,
                "type", "text",
                "text", Map.of("body", messageText)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(WHATSAPP_API_URL, requestEntity, String.class);
            LOGGER.info("WhatsApp API Response: " + response.getStatusCode() + " - " + response.getBody());
        } catch (Exception e) {
            LOGGER.severe("Error sending message to WhatsApp: " + e.getMessage());
        }
    }
}

