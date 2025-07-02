package com.smart24.alpha_robot.network;

import com.smart24.alpha_robot.data.TextToSpeechRequest;
import com.smart24.alpha_robot.data.SpeechToTextResponse;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface GroqApi {
    @Multipart
    @POST("openai/v1/audio/transcriptions")
    Call<SpeechToTextResponse> speechToText(@Header("Authorization") String authorization, @Part MultipartBody.Part file, @Part MultipartBody.Part model);

    @POST("openai/v1/audio/speech")
    Call<ResponseBody> textToSpeech(@Header("Authorization") String authorization, @Body TextToSpeechRequest body);
}
