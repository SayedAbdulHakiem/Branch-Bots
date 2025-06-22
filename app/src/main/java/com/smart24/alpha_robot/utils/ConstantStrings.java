package com.smart24.alpha_robot.utils;

public class ConstantStrings {
    public static final String myPreferenceKey = "alphaRobotPrefKey";
    public static final String appLocaleKey = "appLocaleKey";
    public static final String GROQ_BASE_URL_AUDIO = "https://api.groq.com";
    public static final String GROQ_TRANSCRIPT_MODEL_ID = "whisper-large-v3-turbo";
    public static final String GROQ_TRANSCRIPT_MODEL_ID2 = "distil-whisper-large-v3-en";
    public static final String GROQ_API_KEY = "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";
    public static final String GROQ_API_AUTHORIZATION = "bearer " + GROQ_API_KEY;
    public static final String GROQ_RESPONSE_FORMAT = "json";
    public static final Float GROQ_TEMPERATURE = 0.0f;
    public static final String GROQ_LANGUAGE = "en";
    public static final String[] GROQ_TIMESTAMP_GRANULARITIES = new String[]{"word", "segment"};

    private static ConstantStrings instance;

    public ConstantStrings() {
    }

    public static ConstantStrings getInstance() {
        if (instance == null)
            instance = new ConstantStrings();
        return instance;
    }


}
