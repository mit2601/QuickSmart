package com.example.quicksmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private List<SuggestionModel> list;
    private OnItemClick listener;

    public interface OnItemClick {
        void onClick(SuggestionModel model);
    }

    public SuggestionAdapter(List<SuggestionModel> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    // 🔥 VERY IMPORTANT FIX
    public void updateList(List<SuggestionModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SuggestionModel model = list.get(position);

        if (model.isCurrentLocation()) {
            holder.text.setText("📍 Use Current Location");
        } else {
            holder.text.setText(model.getName());
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(model));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(android.R.id.text1);
        }
    }
}