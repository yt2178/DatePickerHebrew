package com.yt.hebrewdatepicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private int selectedPosition = -1;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener) {
        // If the list is null, create a new one to prevent NullPointerException
        if (daysOfMonth == null) {
            this.daysOfMonth = new ArrayList<>();
        } else {
            this.daysOfMonth = daysOfMonth;
        }
        this.onItemListener = onItemListener;
    }

    public void updateDays(ArrayList<String> newDays) {
        daysOfMonth.clear();
        daysOfMonth.addAll(newDays);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_day_item, parent, false);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        if (dayText.isEmpty()) {
            holder.itemView.setClickable(false);
            holder.dayOfMonth.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setClickable(true);
            holder.dayOfMonth.setVisibility(View.VISIBLE);
        }

        // Highlight the view if its position is the selected position
        holder.itemView.setSelected(position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public void setSelectedPosition(int position) {
        if (selectedPosition == position) return; // No change needed

        int oldPosition = selectedPosition;
        selectedPosition = position;

        // Efficiently update only the old and new selected items
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView dayOfMonth;
        private final OnItemListener onItemListener;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.tv_day_text);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemListener != null) {
                int position = getAdapterPosition();
                String dayText = dayOfMonth.getText().toString();

                // Only trigger click if the position is valid and the cell is not empty
                if (position != RecyclerView.NO_POSITION && !dayText.isEmpty()) {
                    onItemListener.onItemClick(position);
                }
            }
        }
    }
}