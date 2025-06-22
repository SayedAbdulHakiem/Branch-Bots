package com.smart24.alpha_robot.data;


import java.io.File;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChatVoiceMessage extends ChatMessage {
    private File voiceFile;

    public ChatVoiceMessage(File voiceFile) {
        super(null, MessageTypeEnum.VOICE);
        this.voiceFile = voiceFile;
    }

    public ChatVoiceMessage(File voiceFile, String senderId, String senderName) {
        super(null, MessageTypeEnum.VOICE, senderId, senderName);
        this.voiceFile = voiceFile;
    }

}
