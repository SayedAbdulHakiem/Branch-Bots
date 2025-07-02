package com.smart24.alpha_robot.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smart24.alpha_robot.BuildConfig;
import com.smart24.alpha_robot.utils.ConstantStrings;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Smart24RetrofitClient {
    public static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {

        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            OkHttpClient.Builder okHttpBuilder = new OkHttpClient().newBuilder();
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
                okHttpBuilder.addInterceptor(loggingInterceptor);
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(ConstantStrings.SMART24_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpBuilder.build())
                    .build();

        }
        return retrofit;
    }
}
