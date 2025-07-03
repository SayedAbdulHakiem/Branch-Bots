package com.smart24.branch_bots.data;


import java.io.File;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChatVoiceMessage extends ChatMessage {
    private File voiceFile;

    public ChatVoiceMessage(File voiceFile, String optionalText) {
        super(optionalText, MessageTypeEnum.VOICE);
        this.voiceFile = voiceFile;
    }

    public ChatVoiceMessage(File voiceFile, String text, String senderId, String senderName) {
        super(text, MessageTypeEnum.VOICE, senderId, senderName);
        this.voiceFile = voiceFile;
    }

}
