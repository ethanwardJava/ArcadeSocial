package com.arcade.arcadesocial.chat;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessage {

    private String content;
    private String sender;
    private MessageType type;
}