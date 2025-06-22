package com.smart24.alpha_robot.data;


import java.util.Date;

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

    public ChatMessage(String text, MessageTypeEnum messageType) {
        this.text = text;
        this.messageType = messageType;
        senderId = "1";// TODO user sender id
        senderName = "1";// TODO user sender name
        timeStamp = new Date().getTime();
        isSentByUser = true;
    }

    public ChatMessage(String text, MessageTypeEnum messageType, String senderId, String senderName) {
        this.text = text;
        this.messageType = messageType;
        this.senderId = senderId;
        this.senderName = senderName;
        timeStamp = new Date().getTime();
        isSentByUser = false;
    }

}
