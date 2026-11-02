package com.example.cookingingredients.model;

import java.util.ArrayList;
import java.util.List;

public class IngredientCatalog {

    // Danh sách nguyên liệu: tên Việt, tên gửi API, emoji
    private static final String[][] DATA = {
            {"Thịt gà", "chicken", "🍗"},
            {"Thịt bò", "beef", "🥩"},
            {"Cá", "salmon", "🐟"},
            {"Trứng", "egg", "🥚"},
            {"Cà chua", "tomato", "🍅"},
            {"Khoai tây", "potato", "🥔"},
            {"Cà rốt", "carrot", "🥕"},
            {"Bông cải", "broccoli", "🥦"},
            {"Hành tây", "onion", "🧅"},
            {"Tỏi", "garlic", "🧄"},
            {"Ớt", "chilli", "🌶️"},
            {"Nấm", "mushroom", "🍄"}
    };

    // Tạo danh sách mới (selected = false) cho màn Chọn nguyên liệu
    public static List<Ingredient> createAll() {
        List<Ingredient> list = new ArrayList<>();
        for (String[] d : DATA) {
            list.add(new Ingredient(d[0], d[1], d[2]));
        }
        return list;
    }

    // "garlic" -> "🧄 Tỏi". Không tìm thấy thì trả lại nguyên tên gốc
    public static String label(String apiName) {
        for (String[] d : DATA) {
            if (d[1].equalsIgnoreCase(apiName)) {
                return d[2] + " " + d[0];
            }
        }
        return apiName;
    }
}