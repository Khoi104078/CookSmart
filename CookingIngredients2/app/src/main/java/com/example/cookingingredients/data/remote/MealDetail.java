package com.example.cookingingredients.data.remote;

import java.util.ArrayList;
import java.util.List;

public class MealDetail {
    public String idMeal;
    public String strMeal;
    public String strMealThumb;
    public String strInstructions;
    public String strCategory;
    public String strArea;

    public String strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
            strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20;

    public String strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
            strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20;

    // Gộp nguyên liệu + định lượng thành các dòng, bỏ dòng trống
    public List<String> getIngredientLines() {
        List<String> result = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            try {
                String ing = (String) MealDetail.class.getField("strIngredient" + i).get(this);
                String mea = (String) MealDetail.class.getField("strMeasure" + i).get(this);
                if (ing != null && !ing.trim().isEmpty()) {
                    String line = "• " + ing.trim();
                    if (mea != null && !mea.trim().isEmpty()) {
                        line += " - " + mea.trim();
                    }
                    result.add(line);
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }
}