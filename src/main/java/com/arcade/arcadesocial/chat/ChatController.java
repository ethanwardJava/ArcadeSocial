package com.arcade.arcadesocial.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class ChatController {

    /**
     * Handles incoming chat messages sent to "/app/chat.sendMessage".
     * The returned value is broadcast to all subscribers of "/topic/public".
     *
     * @param message incoming chat payload
     * @return the same message (you can enrich it with timestamps, IDs, etc.)
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        // Currently just echoes the message back to all clients.
        return message;
    }

    /**
     * Registers a new user when they join the chat.
     * Stores the username inside the WebSocket session attributes.
     *
     * @param message        chat payload containing the username
     * @param headerAccessor provides access to WebSocket session attributes
     * @return JOIN type message broadcast to everyone
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message,
                               SimpMessageHeaderAccessor headerAccessor) {

        // Extract username and store it in the session map
        String username = message.getSender();
        Objects.requireNonNull(headerAccessor.getSessionAttributes())
                .put("username", username);

        // Modify message type to indicate a join event
        message.setType(MessageType.JOIN);

        return message;
    }

    /**
     * Explicit logout/remove-user endpoint.
     * Called from the frontend when the user leaves the page.
     *
     * @param message payload containing the username
     * @return LEAVE type broadcast
     */
    @MessageMapping("/chat.removeUser")
    @SendTo("/topic/public")
    public ChatMessage removeUser(@Payload ChatMessage message) {

        // Mark this message as a LEAVE event
        message.setType(MessageType.LEAVE);

        return message;
    }
}
