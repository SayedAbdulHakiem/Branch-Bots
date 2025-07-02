package com.smart24.alpha_robot.network;

import com.smart24.alpha_robot.BuildConfig;
import com.smart24.alpha_robot.data.AnswerResponse;
import com.smart24.alpha_robot.data.QuestionRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Smart24Api {

    @POST("/api/v1/prediction/" + BuildConfig.CHAT_FLOW_ID)
    Call<AnswerResponse> askQuestion(@Body QuestionRequest body);
}
