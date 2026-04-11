package com.hejiale.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import static org.springframework.ai.chat.messages.MessageType.ASSISTANT;

@NoArgsConstructor
@Data
public class AiMessageVO {
    private String role;
    private String content;

    public AiMessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER:
                this.role = "user";
                break;
            case ASSISTANT:
                this.role = "assistant";
                break;
            default:
                this.role = "";
                break;
        }

        this.content = message.getText();
    }
}
