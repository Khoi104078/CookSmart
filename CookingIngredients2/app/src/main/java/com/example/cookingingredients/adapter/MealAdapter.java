package com.example.cookingingredients.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingingredients.MealDetailActivity;
import com.example.cookingingredients.R;
import com.example.cookingingredients.data.local.FavoriteManager;
import com.example.cookingingredients.data.remote.MealSummary;

import java.util.List;
import java.util.Map;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private final List<MealSummary> mealList;

    // Chỉ dùng ở màn Kết quả: idMeal -> số nguyên liệu khớp
    private Map<String, Integer> matchCount;
    private int totalIngredients;
    // true = thẻ rộng vừa khít cột (màn Kết quả, Yêu thích); false = rộng 160dp (Trang chủ)
    private boolean fullWidth = false;

    public void setFullWidth(boolean fullWidth) {
        this.fullWidth = fullWidth;
    }

    public MealAdapter(List<MealSummary> mealList) {
        this.mealList = mealList;
    }

    public void setMatchInfo(Map<String, Integer> matchCount, int totalIngredients) {
        this.matchCount = matchCount;
        this.totalIngredients = totalIngredients;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        if (fullWidth) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(lp);
        }
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealSummary meal = mealList.get(position);
        Context context = holder.itemView.getContext();

        holder.txtMealName.setText(meal.getStrMeal());
        // Dòng phụ: loại món • xuất xứ (tự ẩn nếu API không trả về)
        String category = meal.getStrCategory();
        String area = meal.getStrArea();
        String sub = "";
        if (category != null && !category.isEmpty()) sub = category;
        if (area != null && !area.isEmpty()) sub = sub.isEmpty() ? area : sub + " • " + area;
        if (sub.isEmpty()) {
            holder.tvSub.setVisibility(View.GONE);
        } else {
            holder.tvSub.setText(sub);
            holder.tvSub.setVisibility(View.VISIBLE);
        }

        Glide.with(context)
                .load(meal.getStrMealThumb())
                .placeholder(R.drawable.bg_welcome)
                .centerCrop()
                .into(holder.imgMeal);

        // Nhãn "Phù hợp" (chỉ hiện khi có thông tin khớp)
        if (matchCount != null && totalIngredients > 0
                && matchCount.get(meal.getIdMeal()) != null) {
            int count = matchCount.get(meal.getIdMeal());
            int percent = Math.min(100, count * 100 / totalIngredients);
            String text;
            int bg;
            if (percent >= 80) {
                text = "✓ Phù hợp cao · " + percent + "%";
                bg = R.drawable.bg_badge_high;
            } else if (percent >= 50) {
                text = "Phù hợp " + count + "/" + totalIngredients + " · " + percent + "%";
                bg = R.drawable.bg_badge_mid;
            } else {
                text = "Có " + count + "/" + totalIngredients + " · " + percent + "%";
                bg = R.drawable.bg_badge_low;
            }
            holder.tvMatch.setText(text);
            holder.tvMatch.setBackgroundResource(bg);
            holder.tvMatch.setVisibility(View.VISIBLE);
        } else {
            holder.tvMatch.setVisibility(View.GONE);
        }

        // Nút tim
        FavoriteManager favorites = new FavoriteManager(context);
        updateHeart(holder.btnHeart, favorites.isFavorite(meal.getIdMeal()));
        holder.btnHeart.setOnClickListener(v -> {
            boolean isFav = favorites.toggle(meal);
            updateHeart(holder.btnHeart, isFav);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MealDetailActivity.class);
            intent.putExtra("mealId", meal.getIdMeal());
            v.getContext().startActivity(intent);
        });
    }

    private void updateHeart(TextView heart, boolean isFav) {
        heart.setText(isFav ? "♥" : "♡");
        heart.setTextColor(isFav ? Color.parseColor("#E53935") : Color.parseColor("#222222"));
    }

    @Override
    public int getItemCount() {
        return mealList == null ? 0 : mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMeal;
        TextView txtMealName, btnHeart, tvMatch, tvSub;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            txtMealName = itemView.findViewById(R.id.txtMealName);
            btnHeart = itemView.findViewById(R.id.btnHeart);
            tvMatch = itemView.findViewById(R.id.tvMatch);
            tvSub = itemView.findViewById(R.id.tvSub);
        }
    }
}