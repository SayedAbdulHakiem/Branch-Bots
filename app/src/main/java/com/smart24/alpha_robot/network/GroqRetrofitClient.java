package com.smart24.alpha_robot.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smart24.alpha_robot.utils.ConstantStrings;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class GroqRetrofitClient {
    public static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {

        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(ConstantStrings.GROQ_BASE_URL_AUDIO)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

        }
        return retrofit;
    }
}
