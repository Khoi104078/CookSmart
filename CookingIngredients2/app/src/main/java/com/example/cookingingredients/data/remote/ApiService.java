package com.example.cookingingredients.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Tìm kiếm món ăn theo tên
    @GET("search.php")
    Call<MealSummaryResponse> searchMeals(
            @Query("s") String name
    );

    // Lọc món ăn theo nguyên liệu
    @GET("filter.php")
    Call<MealSummaryResponse> filterByIngredient(
            @Query("i") String ingredient
    );

    // Lấy chi tiết món ăn theo ID
    @GET("lookup.php")
    Call<MealDetailResponse> getMealDetail(
            @Query("i") String id
    );
}