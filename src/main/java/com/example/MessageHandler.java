package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Autowired
    private WhatsAppService whatsappService;

    @Value("${whatsapp.api.verification.token}")
    private String verificationToken;

    @PostMapping
    public void handleIncomingMessage(@RequestBody Map<String, Object> incomingMessage) {
        // Log the incoming message
        logger.info("Received message: {}", incomingMessage);

        // Navigate through the nested structure to extract 'from' and 'text'
        Map<String, Object> entry = ((List<Map<String, Object>>) incomingMessage.get("entry")).get(0);
        Map<String, Object> changes = ((List<Map<String, Object>>) entry.get("changes")).get(0);
        Map<String, Object> value = (Map<String, Object>) changes.get("value");
        Map<String, Object> message = ((List<Map<String, Object>>) value.get("messages")).get(0);

        String from = (String) message.get("from");
        String text = (String) ((Map<String, Object>) message.get("text")).get("body");


        if (text != null) {
            String trimmedMessageText = text.trim();
            if ("hi".equalsIgnoreCase(trimmedMessageText)) {
                whatsappService.sendMessage(from, "Hi there, how can I help you today?");
            }
        } else {
            logger.warn("Received a message with null text");
        }
    }

    @GetMapping
    public String verifyWebhook(@RequestParam("hub.mode") String mode,
                                @RequestParam("hub.verify_token") String token,
                                @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verificationToken.equals(token)) {
            logger.warn("Verification successfull");

            return challenge;
        } else {
            return "Verification failed";
        }
    }
}