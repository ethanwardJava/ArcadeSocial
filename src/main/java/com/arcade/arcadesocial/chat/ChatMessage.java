package com.arcade.arcadesocial.chat;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessage {

    //The message
    private String content;

    // WHO SENT IT
    private String sender;

    private MessageType type;

}
