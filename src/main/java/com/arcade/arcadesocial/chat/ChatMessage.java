package com.arcade.arcadesocial.chat;

import lombok.*;

/**
 * Represents a WebSocket chat message exchanged between clients.
 * This model is sent/received as the payload in STOMP frames.
 * <p>
 * Fields:
 *  - content: actual text of the message (optional for JOIN/LEAVE events)
 *  - sender:  username of the client sending the message
 *  - type:    defines what kind of message this is (CHAT, JOIN, LEAVE)
 * <p>
 * Lombok handles constructors, getters/setters, and builder pattern generation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    /** Text message content (it may be null for JOIN/LEAVE). */
    private String content;

    /** Username of the user who sent the message. */
    private String sender;

    /** Type of message: CHAT, JOIN, or LEAVE. */
    private MessageType type;
}
