package com.yt.hebrewdatepicker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HebrewDatePickerDialog extends DialogFragment implements CalendarAdapter.OnItemListener {

    public interface OnDateSetListener {
        void onDateSet(JewishCalendar selectedDate, String formattedDate);
    }
    private OnDateSetListener listener;
    private JewishCalendar selectedDate;
    private JewishCalendar displayedMonth;

    private int startDayOffset;

    private TextView yearText, monthText;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;

    public void setOnDateSetListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hebrew_calendar, null);

        yearText = view.findViewById(R.id.tv_year);
        monthText = view.findViewById(R.id.tv_month);
        calendarRecyclerView = view.findViewById(R.id.calendar_recycler_view);

        ImageView prevYearButton = view.findViewById(R.id.btn_prev_year);
        ImageView nextYearButton = view.findViewById(R.id.btn_next_year);
        ImageView prevMonthButton = view.findViewById(R.id.btn_prev_month);
        ImageView nextMonthButton = view.findViewById(R.id.btn_next_month);

        selectedDate = new JewishCalendar();
        displayedMonth = (JewishCalendar) selectedDate.clone();
        displayedMonth.setJewishDayOfMonth(1);

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        adapter = new CalendarAdapter(new ArrayList<>(), this);
        calendarRecyclerView.setAdapter(adapter);

        prevYearButton.setOnClickListener(v -> changeYear(-1));
        nextYearButton.setOnClickListener(v -> changeYear(1));
        prevMonthButton.setOnClickListener(v -> changeMonth(-1));
        nextMonthButton.setOnClickListener(v -> changeMonth(1));

        builder.setView(view)
                .setPositiveButton("אישור", (dialog, id) -> {
                    if (listener != null) {
                        com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter libraryFormatter =
                                new com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter();
                        libraryFormatter.setHebrewFormat(true);
                        String formattedDate = libraryFormatter.format(selectedDate);
                        listener.onDateSet(selectedDate, formattedDate);
                    }
                })
                .setNegativeButton("ביטול", null);

        updateCalendar();
        return builder.create();
    }

    private void updateCalendar() {
        yearText.setText("ה'" + HebrewDateFormatter.toGematria(displayedMonth.getJewishYear()));
        monthText.setText(HebrewDateFormatter.formatMonth(displayedMonth));

        ArrayList<String> days = new ArrayList<>();
        JewishCalendar temp = (JewishCalendar) displayedMonth.clone();
        temp.setJewishDayOfMonth(1);

        startDayOffset = temp.getDayOfWeek() - 1;

        for (int i = 0; i < startDayOffset; i++) {
            days.add("");
        }

        for (int d = 1; d <= displayedMonth.getDaysInJewishMonth(); d++) {
            days.add(HebrewDateFormatter.toGematria(d));
        }

        adapter.updateDays(days);

        if (isSameMonth(displayedMonth, selectedDate)) {
            int position = selectedDate.getJewishDayOfMonth() + startDayOffset - 1;
            adapter.setSelectedPosition(position);
        } else {
            adapter.setSelectedPosition(-1);
        }
    }

    /**
     * יוצר רשימה מסודרת של קבועי חודש לשנה עברית נתונה.
     * @param year השנה העברית.
     * @return רשימה של מספרים שלמים המייצגים את החודשים ברצף הנכון שלהם.
     */
    private List<Integer> getMonthListForYear(int year) {
        JewishCalendar tempCal = new JewishCalendar(year, JewishCalendar.TISHREI, 1);
        if (tempCal.isJewishLeapYear()) {
            return Arrays.asList(
                    JewishCalendar.TISHREI, JewishCalendar.CHESHVAN, JewishCalendar.KISLEV,
                    JewishCalendar.TEVES, JewishCalendar.SHEVAT, JewishCalendar.ADAR,
                    JewishCalendar.ADAR_II, JewishCalendar.NISSAN, JewishCalendar.IYAR,
                    JewishCalendar.SIVAN, JewishCalendar.TAMMUZ, JewishCalendar.AV, JewishCalendar.ELUL
            );
        } else {
            return Arrays.asList(
                    JewishCalendar.TISHREI, JewishCalendar.CHESHVAN, JewishCalendar.KISLEV,
                    JewishCalendar.TEVES, JewishCalendar.SHEVAT, JewishCalendar.ADAR,
                    JewishCalendar.NISSAN, JewishCalendar.IYAR, JewishCalendar.SIVAN,
                    JewishCalendar.TAMMUZ, JewishCalendar.AV, JewishCalendar.ELUL
            );
        }
    }

    /**
     * משנה את החודש המוצג באמצעות גישה חזקה ומבוססת על רשימה לטיפול בכל מקרי הקצה.
     * @param monthDifference -1 לקודם, 1 למשך הבא.
     */
    private void changeMonth(int monthDifference) {
        int currentYear = displayedMonth.getJewishYear();
        int currentMonth = displayedMonth.getJewishMonth();

        List<Integer> monthList = getMonthListForYear(currentYear);
        int currentIndex = monthList.indexOf(currentMonth);
        int newIndex = currentIndex + monthDifference;

        int newYear = currentYear;
        int newMonth;

        if (newIndex < 0) { // עבר לשנה הקודמת
            newYear--;
            List<Integer> prevYearMonthList = getMonthListForYear(newYear);
            newMonth = prevYearMonthList.get(prevYearMonthList.size() - 1); // בחודש שעברה בשנה הקודמת
        } else if (newIndex >= monthList.size()) { // עבר לשנה הבאה
            newYear++;
            newMonth = JewishCalendar.TISHREI; // החודש הראשון לשנה הבאה
        } else { //עבר באותה שנה
            newMonth = monthList.get(newIndex);
        }

        displayedMonth.setJewishDate(newYear, newMonth, 1);
        updateCalendar();
    }

    /**
     * משנה את השנה המוצגת, תוך הבטחת תוקף חודש (למשל, ADAR II).
     * @param yearDifference -1 לקודם, 1 למשך הבא.
     */
    private void changeYear(int yearDifference) {
        int newYear = displayedMonth.getJewishYear() + yearDifference;
        int currentMonth = displayedMonth.getJewishMonth();

        // אם החודש הנוכחי הוא ADAR II ושנת היעד אינה שנה מעוברת,
        // נפל חזרה לאדר הרגיל.
        if (currentMonth == JewishCalendar.ADAR_II) {
            JewishCalendar tempCal = new JewishCalendar(newYear, JewishCalendar.TISHREI, 1);
            if (!tempCal.isJewishLeapYear()) {
                currentMonth = JewishCalendar.ADAR;
            }
        }

        displayedMonth.setJewishDate(newYear, currentMonth, 1);
        updateCalendar();
    }

    @Override
    public void onItemClick(int position) {
        int day = position - startDayOffset + 1;
        if (day < 1) return;

        selectedDate.setJewishDate(
                displayedMonth.getJewishYear(),
                displayedMonth.getJewishMonth(),
                day
        );

        adapter.setSelectedPosition(position);
    }

    private boolean isSameMonth(JewishCalendar a, JewishCalendar b) {
        if (a == null || b == null) return false;
        return a.getJewishYear() == b.getJewishYear() &&
                a.getJewishMonth() == b.getJewishMonth();
    }
}