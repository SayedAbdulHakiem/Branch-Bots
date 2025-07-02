package com.smart24.alpha_robot.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    private String chatId;
    private String question;
    private boolean streaming;

    public class OverrideConfig {
    }

}
