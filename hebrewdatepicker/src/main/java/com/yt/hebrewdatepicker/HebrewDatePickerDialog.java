package com.yt.hebrewdatepicker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;

public class HebrewDatePickerDialog extends DialogFragment {

    private OnDateSetListener listener;
    private JewishCalendar jewishCalendar;

    public interface OnDateSetListener {
        void onDateSet(int year, int month, int day);
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hebrew_picker, null);

        NumberPicker yearPicker = view.findViewById(R.id.pickerYear);
        NumberPicker monthPicker = view.findViewById(R.id.pickerMonth);
        NumberPicker dayPicker = view.findViewById(R.id.pickerDay);

        HebrewDateFormatter libraryFormatter = new HebrewDateFormatter();
        libraryFormatter.setHebrewFormat(true);

        // --- התיקון הסופי והפשוט לשנים ---
        // This is the new, simpler, and correct way to format the year.
        yearPicker.setFormatter(value -> {
            if (value < 3761) return String.valueOf(value); // Safety check
            // For a valid year, display "ה'" plus the Gematria of the last 3 digits.
            return "ה'" + libraryFormatter.formatHebrewNumber(value % 1000);
        });
        // This one was already working correctly
        dayPicker.setFormatter(libraryFormatter::formatHebrewNumber);
        // ------------------------------------

        jewishCalendar = new JewishCalendar();

        int currentYear = jewishCalendar.getJewishYear();
        yearPicker.setMinValue(currentYear - 100);
        yearPicker.setMaxValue(currentYear + 100);
        yearPicker.setValue(currentYear);
        yearPicker.setWrapSelectorWheel(false);

        updateMonthPicker(monthPicker, dayPicker, currentYear);
        monthPicker.setValue(jewishCalendar.getJewishMonth() - 1);
        dayPicker.setValue(jewishCalendar.getJewishDayOfMonth());

        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateMonthPicker(monthPicker, dayPicker, newVal);
        });

        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateDayPicker(dayPicker, yearPicker.getValue(), newVal + 1);
        });

        builder.setView(view)
                .setTitle("בחר תאריך עברי")
                .setPositiveButton("אישור", (dialog, id) -> {
                    if (listener != null) {
                        int year = yearPicker.getValue();
                        int month = monthPicker.getValue() + 1;
                        int day = dayPicker.getValue();
                        listener.onDateSet(year, month, day);
                    }
                })
                .setNegativeButton("ביטול", (dialog, id) -> HebrewDatePickerDialog.this.getDialog().cancel());

        return builder.create();
    }

    private void updateMonthPicker(NumberPicker monthPicker, NumberPicker dayPicker, int year) {
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);

        JewishCalendar tempCalForLeapYear = new JewishCalendar(year, 1, 1);
        boolean isLeapYear = tempCalForLeapYear.isJewishLeapYear();

        int maxMonth = isLeapYear ? 13 : 12;

        String[] monthNames = new String[maxMonth];
        for (int i = 0; i < maxMonth; i++) {
            monthNames[i] = hdf.formatMonth(new JewishCalendar(year, i + 1, 1));
        }

        monthPicker.setDisplayedValues(null);
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(maxMonth - 1);
        monthPicker.setDisplayedValues(monthNames);

        if (monthPicker.getValue() >= maxMonth) {
            monthPicker.setValue(maxMonth - 1);
        }

        updateDayPicker(dayPicker, year, monthPicker.getValue() + 1);
    }

    private void updateDayPicker(NumberPicker dayPicker, int year, int month) {
        JewishCalendar tempCal = new JewishCalendar(year, month, 1);
        int maxDays = tempCal.getDaysInJewishMonth();

        int currentDay = dayPicker.getValue();
        if (currentDay > maxDays) {
            dayPicker.setValue(maxDays);
        }

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(maxDays);
    }
}