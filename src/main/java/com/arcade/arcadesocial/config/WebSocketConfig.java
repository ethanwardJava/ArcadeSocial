package com.arcade.arcadesocial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures WebSocket messaging using STOMP over WebSocket with SockJS fallback.
 * Enables real-time bidirectional communication between clients and server.
 *
 * <p><strong>Security Note:</strong> In production, {@code setAllowedOriginPatterns("*")} MUST be
 * replaced with specific, trusted origins to prevent unauthorized cross-origin WebSocket connections.</p>
 *
 * <p>Uses a dedicated {@link ThreadPoolTaskScheduler} for message broker heartbeats to ensure
 * reliable connection monitoring and prevent client disconnections due to network inactivity.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registers the "/ws" endpoint as the primary WebSocket/STOMP entry point.
     * <p>
     * SockJS is enabled as a fallback for browsers that do not support native WebSocket.
     * This ensures broader client compatibility (e.g., older browsers or restricted environments).
     * </p>
     * <p>
     * <strong>Development-only:</strong> {@code setAllowedOriginPatterns("*")} permits connections
     * from any origin. <strong>This must be restricted in production</strong> using specific domains
     * or environment-based configuration to mitigate CSRF and unauthorized access risks.
     * </p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // TODO: Restrict in production (e.g., via application.yml)
                .withSockJS();
    }

    /**
     * Configures the message broker for publish-subscribe messaging.
     * <ul>
     *   <li>Client messages destined for the server should be prefixed with "/app"</li>
     *   <li>Server-to-client subscription topics are prefixed with "/topic"</li>
     *   <li>Heartbeat is enabled (10s interval) to detect dropped connections</li>
     * </ul>
     * <p>
     * The custom {@link ThreadPoolTaskScheduler} ensures heartbeats are sent reliably even under load.
     * </p>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic")
                .setTaskScheduler(heartbeatScheduler())
                .setHeartbeatValue(new long[]{10000, 10000}); // 10s inbound/outbound
    }

    /**
     * Dedicated thread pool for WebSocket heartbeats.
     * <p>
     * Required when using {@code enableSimpleBroker} with heartbeats.
     * A pool size of 2 is sufficient: one thread for scheduling, one for execution.
     * </p>
     * <p>
     * Thread names are prefixed for easier identification in thread dumps and monitoring tools.
     * </p>
     *
     * @return initialized {@link ThreadPoolTaskScheduler} bean
     */
    @Bean
    public ThreadPoolTaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.initialize(); // Explicit init ensures readiness on startup
        return scheduler;
    }
}