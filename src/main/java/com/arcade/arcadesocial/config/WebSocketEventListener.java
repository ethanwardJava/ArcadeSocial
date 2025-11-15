package com.arcade.arcadesocial.config;

import com.arcade.arcadesocial.chat.ChatMessage;
import com.arcade.arcadesocial.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listens to WebSocket lifecycle events (connect/disconnect) and broadcasts user status updates
 * to all subscribers of the public chat topic.
 *
 * <p>
 * This component enhances user experience by notifying others when a participant joins or leaves
 * the chat, enabling real-time presence awareness.
 * </p>
 *
 * <p><strong>Thread Safety:</strong> All operations are safe as Spring manages event publishing
 * on the client inbound channel, which runs in a single thread per session.</p>
 *
 * @see SessionDisconnectEvent
 * @see SimpMessageSendingOperations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    /**
     * Used to send messages to subscribed clients via STOMP destinations.
     */
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Handles client disconnection events triggered when a WebSocket session is closed
     * (e.g., tab close, network loss, or explicit disconnect).
     *
     * <p>
     * Extracts the {@code username} from session attributes (set during authentication/handshake)
     * and broadcasts a {@link MessageType#LEAVE} message to {@code /topic/public}.
     * </p>
     *
     * <p><strong>Important:</strong> Session attributes may be {@code null} if the client
     * disconnected before completing the handshake or authentication.</p>
     *
     * @param event the disconnection event containing session details
     */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        var sessionAttrs = headerAccessor.getSessionAttributes();

        if (sessionAttrs == null) {
            log.debug("Disconnect event with null session attributes - likely pre-authentication closure.");
            return;
        }

        String username = (String) sessionAttrs.get("username");
        if (username != null && !username.isBlank()) {
            log.info("User disconnected: {}", username);

            ChatMessage leaveMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .build();

            // Broadcast to all subscribers of the public chat room
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        } else {
            log.debug("Disconnect event without valid username in session attributes.");
        }
    }

}