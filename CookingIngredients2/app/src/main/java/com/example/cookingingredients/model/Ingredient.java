package com.example.cookingingredients.model;

public class Ingredient {
    public String name;      // tên hiển thị: "Thịt gà"
    public String apiName;   // tên gửi lên API: "chicken"
    public String emoji;     // tạm dùng emoji thay hình
    public boolean selected;

    public Ingredient(String name, String apiName, String emoji) {
        this.name = name;
        this.apiName = apiName;
        this.emoji = emoji;
    }
}