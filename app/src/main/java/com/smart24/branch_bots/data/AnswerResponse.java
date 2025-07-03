package com.smart24.branch_bots.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
    private String chatMessageId;
    private String chatId;
    private String sessionId;
    private String text;
    private String question;
    private String memoryType;
    private boolean isStreamValid;
}
