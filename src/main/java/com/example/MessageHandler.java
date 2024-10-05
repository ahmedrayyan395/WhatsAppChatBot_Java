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
        logger.info("Received message: {}", incomingMessage);

        List<Map<String, Object>> entries = (List<Map<String, Object>>) incomingMessage.get("entry");
        if (entries != null && !entries.isEmpty()) {
            Map<String, Object> entry = entries.get(0);
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
            if (changes != null && !changes.isEmpty()) {
                Map<String, Object> change = changes.get(0);
                Map<String, Object> value = (Map<String, Object>) change.get("value");

                if (value.containsKey("messages")) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                    for (Map<String, Object> message : messages) {
                        String from = (String) message.get("from");
                        Map<String, Object> interactive = (Map<String, Object>) message.get("interactive");
                        if (interactive != null) {
                            Map<String, Object> buttonReply = (Map<String, Object>) interactive.get("button_reply");
                            if (buttonReply != null) {
                                String buttonId = (String) buttonReply.get("id");
                                handleButtonClick(from, buttonId);
                            }
                        } else {
                            whatsappService.sendInteractiveMessage(from);
                        }
                    }
                }
            }
        }
    }

    private void handleButtonClick(String from, String buttonId) {
        switch (buttonId) {
            case "services":
                whatsappService.sendResponseWithBackButton(from, "Here are our services: [Dummy Data]");
                break;
            case "location":
                whatsappService.sendResponseWithBackButton(from, "Our location is: [Dummy Data]");
                break;
            case "contact":
                whatsappService.sendResponseWithBackButton(from, "Contact us at: [Dummy Data]");
                break;
            case "main_menu":
                whatsappService.sendInteractiveMessage(from);
                break;
            default:
                whatsappService.sendInteractiveMessage(from);
                break;
        }
    }

    @GetMapping
    public String verifyWebhook(@RequestParam("hub.mode") String mode,
                                @RequestParam("hub.verify_token") String token,
                                @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verificationToken.equals(token)) {
            logger.warn("Verification successful");
            return challenge;
        } else {
            return "Verification failed";
        }
    }
}