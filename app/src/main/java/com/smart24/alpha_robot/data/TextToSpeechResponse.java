package com.smart24.alpha_robot.data;

import com.google.gson.annotations.SerializedName;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TextToSpeechResponse {
    private File text;

    @SerializedName("x_groq")
    private XGroq xGroq;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class XGroq {
        private String id;
    }
}
