package com.arcade.arcadesocial.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        // simple echo – you can add timestamp, validation, etc.
        return message;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = message.getSender();
        Objects.requireNonNull(headerAccessor.getSessionAttributes())
                .put("username", username);
        message.setType(MessageType.JOIN);
        return message;
    }

    /* explicit removeUser endpoint (called from JS on unload) */
    @MessageMapping("/chat.removeUser")
    @SendTo("/topic/public")
    public ChatMessage removeUser(@Payload ChatMessage message) {
        message.setType(MessageType.LEAVE);
        return message;
    }
}