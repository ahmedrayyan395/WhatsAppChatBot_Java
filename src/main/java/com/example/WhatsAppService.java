package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;



import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


@Service
public class WhatsAppService {

    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;

    @Value("${whatsapp.api.token}")
    private String whatsappApiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String recipientPhoneNumber, String message) {
    // Validate the recipient phone number
    if (recipientPhoneNumber == null || recipientPhoneNumber.isEmpty()) {
        throw new IllegalArgumentException("Recipient phone number is required");
    }

    String url = whatsappApiUrl;

    // Construct JSON payload using JSONObject
    JSONObject payload = new JSONObject();
    payload.put("messaging_product", "whatsapp");
    payload.put("to", recipientPhoneNumber); // Ensure 'to' field is added
    payload.put("type", "text");

    JSONObject text = new JSONObject();
    text.put("body", message);
    payload.put("text", text);

    // Convert JSONObject to String
    String payloadString = payload.toString();

    // Set up headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(whatsappApiToken); // Set the Authorization header

    // Create the HTTP entity
    HttpEntity<String> entity = new HttpEntity<>(payloadString, headers);

    try {
        // Send the request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Handle the response
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Message sent successfully");
        } else {
            System.out.println("Failed to send message: " + response.getStatusCode());
        }
    } catch (HttpClientErrorException e) {
        // Log error details
        System.err.println("Error sending message: " + e.getResponseBodyAsString());
        throw e; // Re-throw the exception if needed
    }
}

}