package com.example.evenue.utils;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;

public class AccessTokenGenerator {

    /**
     * Generates an access token for authenticating with Google Cloud APIs.
     *
     * @return the access token as a String
     * @throws IOException if there is an issue reading the service account key file
     */
    public static String getAccessToken() throws IOException {
        // Specify the path to your service account key file
        String keyFilePath = "/Users/olawale/Desktop/EVENUE-main/src/main/resources/steppa-chatbot-pdol-e2a7d3053b40.json";

        // Load the service account credentials from the key file
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(keyFilePath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        // Refresh the token if it's expired or not yet created
        credentials.refreshIfExpired();

        // Return the access token
        return credentials.getAccessToken().getTokenValue();
    }

    public static void main(String[] args) {
        try {
            // Generate and print the access token
            String accessToken = getAccessToken();
            System.out.println("Access Token: " + accessToken);
        } catch (IOException e) {
            // Handle exceptions related to file reading or token generation
            System.err.println("Error generating access token: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
