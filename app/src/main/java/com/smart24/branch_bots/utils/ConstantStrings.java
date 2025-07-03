package com.smart24.branch_bots.utils;

import com.smart24.branch_bots.BuildConfig;

public class ConstantStrings {
    public static final String myPreferenceKey = "BranchBotPrefKey";
    public static final String appLocaleKey = "appLocaleKey";
    public static final String GROQ_BASE_URL = "https://api.groq.com";
    public static final String SMART24_BASE_URL = "https://ns1.smart24services.com";
    public static final String GROQ_TRANSCRIPT_MODEL_ID = "whisper-large-v3-turbo";
    public static final String GROQ_SPEECH_MODEL_ID = "playai-tts-arabic";
    public static final String GROQ_SPEECH_VOICE = "Nasser-PlayAI";
    public static final String GROQ_SPEECH_RESPONSE_TYPE = "mp3";
    public static final String GROQ_API_AUTHORIZATION = "Bearer " + BuildConfig.GROQ_API_KEY;
    public static final String SMART24_CHAT_FLOW_ID = BuildConfig.CHAT_FLOW_ID;
    public static final String GROQ_RESPONSE_FORMAT = "json";
    public static final Float GROQ_TEMPERATURE = 0.0f;
    public static final String GROQ_LANGUAGE = "en";
    public static final String[] GROQ_TIMESTAMP_GRANULARITIES = new String[]{"word", "segment"};
    public static final String SPEECH_FILE_NAME = "Speech_";

    private static ConstantStrings instance;

    public ConstantStrings() {
    }

    public static ConstantStrings getInstance() {

        if (instance == null)
            instance = new ConstantStrings();
        return instance;
    }


}
