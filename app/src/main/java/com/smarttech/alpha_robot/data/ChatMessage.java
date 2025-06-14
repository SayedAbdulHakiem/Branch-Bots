package com.smarttech.alpha_robot.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String text;
    private MessageTypeEnum messageType;
    private String senderId;
    private String senderName;
    private long timeStamp;
    private boolean isSentByUser;

}
