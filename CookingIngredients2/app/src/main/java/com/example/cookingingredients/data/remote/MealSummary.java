package com.example.cookingingredients.data.remote;

public class MealSummary {

    private String idMeal;
    private String strMeal;
    private String strMealThumb;
    // Chỉ có khi lấy từ API tìm kiếm; có thể null
    private String strCategory;
    private String strArea;

    public MealSummary() {
    }

    public MealSummary(String idMeal, String strMeal, String strMealThumb) {
        this.idMeal = idMeal;
        this.strMeal = strMeal;
        this.strMealThumb = strMealThumb;
    }

    public String getIdMeal() {
        return idMeal;
    }

    public String getStrMeal() {
        return strMeal;
    }

    public String getStrMealThumb() {
        return strMealThumb;
    }

    public String getStrCategory() {
        return strCategory;
    }

    public String getStrArea() {
        return strArea;
    }
}