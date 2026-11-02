package com.example.cookingingredients.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL =
            "https://www.themealdb.com/api/json/v1/1/";

    private static Retrofit retrofit;

    public static ApiService getApiService() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}