package com.smart24.branch_bots.network;

import com.smart24.branch_bots.BuildConfig;
import com.smart24.branch_bots.data.AnswerResponse;
import com.smart24.branch_bots.data.QuestionRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SmartTechApi {

    @POST("/api/v1/prediction/" + BuildConfig.CHAT_FLOW_ID)
    Call<AnswerResponse> askQuestion(@Body QuestionRequest body);
}
