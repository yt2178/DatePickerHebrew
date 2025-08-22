package com.yt.hebrewdatepicker;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    public static class DayData {
        final String dayText;
        final boolean isDisabled;

        DayData(String dayText, boolean isDisabled) {
            this.dayText = dayText;
            this.isDisabled = isDisabled;
        }
    }

    private final ArrayList<DayData> daysOfMonth;
    private final OnItemListener onItemListener;
    private int selectedPosition = -1;

    public CalendarAdapter(ArrayList<DayData> daysOfMonth, OnItemListener onItemListener) {
        this.daysOfMonth = (daysOfMonth == null) ? new ArrayList<>() : daysOfMonth;
        this.onItemListener = onItemListener;
    }

    public void updateDays(ArrayList<DayData> newDays) {
        daysOfMonth.clear();
        daysOfMonth.addAll(newDays);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_day_item, parent, false);
        return new CalendarViewHolder(view, onItemListener, daysOfMonth);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayData dayData = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayData.dayText);

        if (dayData.dayText.isEmpty()) {
            holder.itemView.setClickable(false);
            holder.dayOfMonth.setVisibility(View.INVISIBLE);
        } else {
            holder.dayOfMonth.setVisibility(View.VISIBLE);
            if (dayData.isDisabled) {
                holder.itemView.setClickable(false);
                holder.dayOfMonth.setAlpha(0.4f);
//                holder.dayOfMonth.setPaintFlags(holder.dayOfMonth.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.itemView.setClickable(true);
                holder.dayOfMonth.setAlpha(1.0f);
//                holder.dayOfMonth.setPaintFlags(holder.dayOfMonth.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
        holder.itemView.setSelected(position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public void setSelectedPosition(int position) {
        if (selectedPosition == position) return;
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (selectedPosition != -1) notifyItemChanged(selectedPosition);
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView dayOfMonth;
        private final OnItemListener onItemListener;
        private final ArrayList<DayData> daysRef;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<DayData> daysRef) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.tv_day_text);
            this.onItemListener = onItemListener;
            this.daysRef = daysRef;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (onItemListener != null && position != RecyclerView.NO_POSITION) {
                DayData dayData = daysRef.get(position);
                if (!dayData.dayText.isEmpty() && !dayData.isDisabled) {
                    onItemListener.onItemClick(position);
                }
            }
        }
    }
}