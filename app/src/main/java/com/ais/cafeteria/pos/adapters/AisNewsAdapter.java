package com.ais.cafeteria.pos.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.models.AisNewsItem;

import java.util.ArrayList;
import java.util.List;

public class AisNewsAdapter extends RecyclerView.Adapter<AisNewsAdapter.NewsViewHolder> {

    private final List<AisNewsItem> items = new ArrayList<>();

    public void submitItems(List<AisNewsItem> newsItems) {
        items.clear();
        items.addAll(newsItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_feed, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        AisNewsItem item = items.get(position);
        holder.tvTag.setText(item.getTag().isEmpty() ? "AIS Update" : item.getTag());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDate.setText(item.getPublishedDate().isEmpty() ? "Recent update" : item.getPublishedDate());
        holder.tvSummary.setText(item.getSummary().isEmpty()
                ? "Open the article to read the full AIS update."
                : item.getSummary());

        View.OnClickListener openArticle = v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getArticleUrl()));
            v.getContext().startActivity(browserIntent);
        };

        holder.itemView.setOnClickListener(openArticle);
        holder.btnReadMore.setOnClickListener(openArticle);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTag;
        final TextView tvTitle;
        final TextView tvDate;
        final TextView tvSummary;
        final Button btnReadMore;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvNewsTag);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvDate = itemView.findViewById(R.id.tvNewsDate);
            tvSummary = itemView.findViewById(R.id.tvNewsSummary);
            btnReadMore = itemView.findViewById(R.id.btnReadMore);
        }
    }
}
