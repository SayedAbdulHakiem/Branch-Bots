package com.smart24.alpha_robot.network;

import com.smart24.alpha_robot.data.TranscribedResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface GroqApi {
    @Multipart
    @POST("openai/v1/audio/transcriptions")
    Call<TranscribedResponse> transcribeAudio(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part file,
            @Part("model") String modelId,
            @Part("temperature") Float temperature,
            @Part("response_format") String responseFormat,
            @Part("timestamp_granularities") String[] timestampGranularities,
            @Part("language") String language
    );


    @Multipart
    @POST("openai/v1/audio/transcriptions")
    Call<TranscribedResponse> transcribeAudio(@Header("Authorization") String authorization, @Part MultipartBody.Part file, @Part("model") String model);
}
