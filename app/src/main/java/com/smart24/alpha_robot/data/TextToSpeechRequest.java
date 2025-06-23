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
public class TextToSpeechRequest {
    @SerializedName("model")
    private String model;

    @SerializedName("input")
    private String input;

    @SerializedName("voice")
    private String voice;

    @SerializedName("response_format")
    private String responseFormat; // e.g., "mp3", "wav"

}
