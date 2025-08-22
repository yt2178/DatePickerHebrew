package com.yt.hebrewdatepicker;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HebrewDatePickerDialog extends DialogFragment implements CalendarAdapter.OnItemListener {
    private boolean disablePastDates = true;
    private boolean disableHolidaysAndShabbat = false;

    private OnDateSetListener listener;
    private JewishCalendar selectedDate;
    private JewishCalendar displayedMonth;
    private JewishCalendar today;

    private int startDayOffset;

    private ImageView prevYearButton, nextYearButton, prevMonthButton, nextMonthButton;
    private TextView yearText, monthText;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;

    public interface OnDateSetListener {
        void onDateSet(JewishCalendar selectedDate, String formattedDate);
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    public static class Builder {
        private boolean disablePast = false;
        private boolean disableHolidays = false;
        private OnDateSetListener dateSetListener;

        public Builder setDisablePastDates(boolean disable) {
            this.disablePast = disable;
            return this;
        }

        public Builder setDisableHolidaysAndShabbat(boolean disable) {
            this.disableHolidays = disable;
            return this;
        }

        public Builder setOnDateSetListener(OnDateSetListener listener) {
            this.dateSetListener = listener;
            return this;
        }

        public HebrewDatePickerDialog build() {
            HebrewDatePickerDialog dialog = new HebrewDatePickerDialog();
            Bundle args = new Bundle();
            args.putBoolean("disablePast", disablePast);
            args.putBoolean("disableHolidays", disableHolidays);
            dialog.setArguments(args);
            dialog.setOnDateSetListener(dateSetListener);
            return dialog;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            disablePastDates = getArguments().getBoolean("disablePast", false);
            disableHolidaysAndShabbat = getArguments().getBoolean("disableHolidays", false);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hebrew_calendar, null);
        dialog.setContentView(view);

        yearText = view.findViewById(R.id.tv_year);
        monthText = view.findViewById(R.id.tv_month);
        calendarRecyclerView = view.findViewById(R.id.calendar_recycler_view);
        prevYearButton = view.findViewById(R.id.btn_prev_year);
        nextYearButton = view.findViewById(R.id.btn_next_year);
        prevMonthButton = view.findViewById(R.id.btn_prev_month);
        nextMonthButton = view.findViewById(R.id.btn_next_month);
        Button jumpButton = view.findViewById(R.id.btn_jump_to_date);
        Button okButton = view.findViewById(R.id.btn_ok);
        Button cancelButton = view.findViewById(R.id.btn_cancel);


        today = new JewishCalendar();
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

        yearText.setOnClickListener(v -> showYearSelectionDialog());
        monthText.setOnClickListener(v -> showMonthSelectionDialog());

        // Set listeners for action buttons
        jumpButton.setOnClickListener(v -> showJumpToDateDialog());
        okButton.setOnClickListener(v -> {
            if (listener != null) {
                String formattedDate = HebrewDateFormatter.format(selectedDate);
                listener.onDateSet(selectedDate, formattedDate);
            }
            dismiss();
        });
        cancelButton.setOnClickListener(v -> dismiss());

        updateCalendar();
        return dialog;
    }

    private void updateCalendar() {
        yearText.setText("ה'" + HebrewDateFormatter.toGematria(displayedMonth.getJewishYear() % 1000));
        monthText.setText(HebrewDateFormatter.formatMonth(displayedMonth));

        ArrayList<CalendarAdapter.DayData> days = new ArrayList<>();
        JewishCalendar temp = (JewishCalendar) displayedMonth.clone();
        temp.setJewishDayOfMonth(1);
        startDayOffset = temp.getDayOfWeek() - 1;

        for (int i = 0; i < startDayOffset; i++) {
            days.add(new CalendarAdapter.DayData("", false));
        }

        for (int d = 1; d <= displayedMonth.getDaysInJewishMonth(); d++) {
            temp.setJewishDayOfMonth(d);
            boolean isDisabled = false;

            if (disablePastDates && isBeforeToday(temp)) {
                isDisabled = true;
            }

            int yomTovIndex = temp.getYomTovIndex();
            boolean isYomTovOrShabbat = (yomTovIndex != -1 && yomTovIndex != JewishCalendar.YOM_KIPPUR) || temp.getDayOfWeek() == 7;
            boolean isFastDay = temp.isTaanis();
            if (disableHolidaysAndShabbat && (isYomTovOrShabbat || isFastDay)) {
                isDisabled = true;
            }
            days.add(new CalendarAdapter.DayData(HebrewDateFormatter.toGematria(d), isDisabled));
        }
        adapter.updateDays(days);
        if (isSameMonth(displayedMonth, selectedDate)) {
            int position = selectedDate.getJewishDayOfMonth() + startDayOffset - 1;
            adapter.setSelectedPosition(position);
        } else {
            adapter.setSelectedPosition(-1);
        }
        updateNavigationButtonsVisibility();
    }
    private void updateNavigationButtonsVisibility() {
        if (disablePastDates) {
            // Check if the displayed month is the current month or in the future
            boolean canGoBack = isAfterCurrentMonth(displayedMonth);
            prevMonthButton.setVisibility(canGoBack ? View.VISIBLE : View.INVISIBLE);
            prevYearButton.setVisibility(canGoBack ? View.VISIBLE : View.INVISIBLE);
        } else {
            // Always show buttons if past dates are not disabled
            prevMonthButton.setVisibility(View.VISIBLE);
            prevYearButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAfterCurrentMonth(JewishCalendar date) {
        int todayYear = today.getJewishYear();
        int todayMonth = today.getJewishMonth();
        int dateYear = date.getJewishYear();
        int dateMonth = date.getJewishMonth();

        if (dateYear > todayYear) return true;
        if (dateYear == todayYear && dateMonth > todayMonth) return true;
        return false;
    }

    private void showYearSelectionDialog() {
        int currentYear = displayedMonth.getJewishYear();
        int startYear = disablePastDates ? today.getJewishYear() : currentYear - 50;
        int endYear = currentYear + 50;

        ArrayList<String> yearList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            yearList.add("ה'" + HebrewDateFormatter.toGematria(i % 1000));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("בחר שנה")
                .setItems(yearList.toArray(new String[0]), (d, which) -> {
                    int selectedYear = startYear + which;
                    displayedMonth.setJewishYear(selectedYear);
                    updateCalendar();
                })
                .show();
    }

    private void showMonthSelectionDialog() {
        int currentYear = displayedMonth.getJewishYear();
        List<Integer> monthConstants = getMonthListForYear(currentYear);
        ArrayList<String> monthNames = new ArrayList<>();

        // Create a temporary formatter for this, since the member one might have different settings
        com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter hdf = new com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter();
        hdf.setHebrewFormat(true);

        for (int monthConst : monthConstants) {
            // If past dates are disabled, only add months from the current month onwards
            if (disablePastDates && currentYear == today.getJewishYear() && monthConst < today.getJewishMonth()) {
                continue;
            }
            monthNames.add(hdf.formatMonth(new JewishCalendar(currentYear, monthConst, 1)));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("בחר חודש")
                .setItems(monthNames.toArray(new String[0]), (d, which) -> {
                    // We need to find the correct constant from the filtered list
                    int selectedMonthConstant = -1;
                    int count = 0;
                    for (int monthConst : monthConstants) {
                        if (disablePastDates && currentYear == today.getJewishYear() && monthConst < today.getJewishMonth()) {
                            continue;
                        }
                        if(count == which){
                            selectedMonthConstant = monthConst;
                            break;
                        }
                        count++;
                    }
                    if(selectedMonthConstant != -1) {
                        displayedMonth.setJewishMonth(selectedMonthConstant);
                        updateCalendar();
                    }
                })
                .show();
    }
    /**
     * מציג דיאלוג המאפשר למשתמש לקפוץ לחודש ושנה ספציפיים באמצעות חיפוש טקסט.
     * <p>
     * ההצעות מסוננות כדי להציג רק תאריכים עתידיים אם האפשרות {@code disablePastDates} מופעלת.
     */
    private void showJumpToDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_jump_to_date, null);

        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.actv_month_year);
        TextView tvInstructions = dialogView.findViewById(R.id.tv_instructions);
        tvInstructions.setText("הקלד את החודש והשנה (למשל: תשרי תשפ''ד). ניתן גם להזין רק את השנה, והמערכת תציע את כל החודשים שלה.");

        ArrayList<String> allSuggestions = new ArrayList<>();
        Map<String, JewishCalendar> monthMap = new HashMap<>();

        // יצירת רשימת חודשים ל-20 שנה קדימה
        JewishCalendar cal = (JewishCalendar) displayedMonth.clone();
        cal.setJewishDayOfMonth(1);
        int monthsToShow = 600; // 50 שנה קדימה
        for (int i = 0; i < monthsToShow; i++) {
            int year = cal.getJewishYear();
            int yearGematria = year % 1000;
            String yearText = HebrewDateFormatter.toGematria(yearGematria);
            if (!yearText.startsWith("ת")) yearText = "ת" + yearText;

            String suggestionText = HebrewDateFormatter.formatMonth(cal) + " " + yearText;
            allSuggestions.add(suggestionText);
            monthMap.put(suggestionText, (JewishCalendar) cal.clone());

            int currentYear = cal.getJewishYear();
            int currentMonth = cal.getJewishMonth();
            List<Integer> monthList = getMonthListForYear(currentYear);
            int currentIndex = monthList.indexOf(currentMonth);
            int newIndex = currentIndex + 1;
            int newYear = currentYear;
            int newMonth;
            if (newIndex >= monthList.size()) {
                newYear++;
                newMonth = JewishCalendar.TISHREI;
            } else {
                newMonth = monthList.get(newIndex);
            }
            cal.setJewishDate(newYear, newMonth, 1);
        }

        // Adapter מותאם אישית עם פילטר
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(allSuggestions)
        ) {
            private final List<String> all = new ArrayList<>(allSuggestions);

            private final Filter containsFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<String> out = new ArrayList<>();
                    String q = normalizeHebrew(constraint);
                    if (q.isEmpty()) {
                        out.addAll(all);
                    } else {
                        for (String s : all) {
                            if (normalizeHebrew(s).contains(q)) {
                                out.add(s);
                            }
                        }
                    }
                    results.values = out;
                    results.count = out.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    //noinspection unchecked
                    addAll((List<String>) results.values);
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return (String) resultValue;
                }
            };

            @Override
            public Filter getFilter() {
                return containsFilter;
            }
        };

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(0); // אפס = הצעות גם בלי הקלדה


        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                if (s != null && s.length() >= 1 && !autoCompleteTextView.isPopupShowing()) {
                    autoCompleteTextView.showDropDown();
                }
            }
        });

        autoCompleteTextView.setOnClickListener(v -> {
            if (!autoCompleteTextView.isPopupShowing()) {
                autoCompleteTextView.showDropDown();
            }
        });

        builder.setView(dialogView)
                .setTitle("קפיצה לחודש ושנה")
                .setPositiveButton("אישור", (d, which) -> {
                    String input = autoCompleteTextView.getText().toString().trim();
                    if (input.isEmpty()) return;
                    // We check if the user's input EXACTLY matches one of the suggestions.
                    JewishCalendar jumpToDate = monthMap.get(input);

                    if (jumpToDate != null) {
                        // A valid selection was made from the suggestion list. Jump to it.
                        displayedMonth.setJewishDate(jumpToDate.getJewishYear(),
                                jumpToDate.getJewishMonth(), 1);
                        selectedDate.setJewishDate(jumpToDate.getJewishYear(),
                                jumpToDate.getJewishMonth(), 1);
                        updateCalendar();
                    } else {
                        // User typed something that is not in the list, or a partial text.
                        Toast.makeText(getContext(),
                                "אנא בחר תאריך חוקי מהרשימה",
                                Toast.LENGTH_SHORT).show();
                    }

//                    String normalizedInput = normalizeHebrew(input);
//                    JewishCalendar jumpToDate = null;
//
//                    for (String key : monthMap.keySet()) {
//                        if (normalizeHebrew(key).contains(normalizedInput)) {
//                            jumpToDate = monthMap.get(key);
//                            break;
//                        }
//                    }
//
//                    if (jumpToDate != null) {
//                        String[] inputParts = input.trim().split("\\s+");
//                        if (inputParts.length == 1) {
//                            int targetYear = jumpToDate.getJewishYear();
//                            List<String> monthsOfYear = new ArrayList<>();
//                            Map<String, JewishCalendar> tempMap = new HashMap<>();
//
//                            for (String key : monthMap.keySet()) {
//                                JewishCalendar jc = monthMap.get(key);
//                                if (jc.getJewishYear() == targetYear) {
//                                    monthsOfYear.add(key);
//                                    tempMap.put(key, jc);
//                                }
//                            }
//
//                            Map<String, Integer> monthOrderMap = new HashMap<>();
//                            monthOrderMap.put("תשרי", 1);
//                            monthOrderMap.put("מר חשוון", 2);
//                            monthOrderMap.put("חשוון", 2);
//                            monthOrderMap.put("כסלו", 3);
//                            monthOrderMap.put("טבת", 4);
//                            monthOrderMap.put("שבט", 5);
//                            monthOrderMap.put("אדר", 6);
//                            monthOrderMap.put("אדר א׳", 6);
//                            monthOrderMap.put("אדר ב׳", 7);
//                            monthOrderMap.put("ניסן", 8);
//                            monthOrderMap.put("אייר", 9);
//                            monthOrderMap.put("סיון", 10);
//                            monthOrderMap.put("תמוז", 11);
//                            monthOrderMap.put("אב", 12);
//                            monthOrderMap.put("אלול", 13);
//
//                            // Use Collections.sort which is compatible with API 19+
//                            Collections.sort(monthsOfYear, (a, b) -> {
//                                String monthA = a.split(" ")[0];
//                                String monthB = b.split(" ")[0];
//                                Integer orderA = monthOrderMap.get(monthA);
//                                Integer orderB = monthOrderMap.get(monthB);
//
//                                // Safety check in case of unexpected month names
//                                if (orderA == null || orderB == null) {
//                                    return 0;
//                                }
//                                return orderA.compareTo(orderB);
//                            });
//
//                            AlertDialog.Builder monthBuilder = new AlertDialog.Builder(requireContext());
//                            monthBuilder.setTitle("בחר חודש לשנה " + input)
//                                    .setItems(monthsOfYear.toArray(new String[0]), (dialog1, whichMonth) -> {
//                                        JewishCalendar selectedMonth = tempMap.get(monthsOfYear.get(whichMonth));
//                                        displayedMonth.setJewishDate(selectedMonth.getJewishYear(),
//                                                selectedMonth.getJewishMonth(), 1);
//                                        selectedDate.setJewishDate(selectedMonth.getJewishYear(),
//                                                selectedMonth.getJewishMonth(), 1);
//                                        updateCalendar();
//                                    })
//                                    .show();
//                            return;
//                        }
//
//                        displayedMonth.setJewishDate(jumpToDate.getJewishYear(),
//                                jumpToDate.getJewishMonth(), 1);
//                        selectedDate.setJewishDate(jumpToDate.getJewishYear(),
//                                jumpToDate.getJewishMonth(), 1);
//                        updateCalendar();
//                    } else {
//                        Toast.makeText(getContext(),
//                                "אנא הזן תאריך חוקי מהרשימה או שנה בגימטריה",
//                                Toast.LENGTH_SHORT).show();
//                    }
                })
                .setNegativeButton("ביטול", null);

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            // This flag tells the window to RESIZE itself when the keyboard appears.
            // The ScrollView we added will then handle the scrolling.
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();

        // טיפול בלחצן BACK — סגירה מסודרת של popup, מקלדת והדיאלוג
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                // הורד את הפוקוס מהשדה (מניע החזקה של המקלדת)
                autoCompleteTextView.clearFocus();

                // סגור את ה-popup מיד אם פתוח
                if (autoCompleteTextView.isPopupShowing()) {
                    autoCompleteTextView.dismissDropDown();
                }

                // סגור את המקלדת (ננסה גם על ה-windowToken של הדיאלוג)
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
                    if (dialog.getWindow() != null && dialog.getWindow().getDecorView() != null) {
                        imm.hideSoftInputFromWindow(dialog.getWindow().getDecorView().getWindowToken(), 0);
                    }
                }

                // לבסוף - סגור את הדיאלוג עצמו
                dialog.dismiss();

                // עצור את אירוע ה-BACK כדי שהמערכת לא תטפל בו שוב
                return true;
            }
            return false;
        });
        dialog.setOnDismissListener(dialogInterface -> {
            autoCompleteTextView.clearFocus();
            if (autoCompleteTextView.isPopupShowing()) {
                autoCompleteTextView.dismissDropDown();
            }
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
            }
        });
    }

    // פונקציה נפרדת לנרמול
    private String normalizeHebrew(CharSequence cs) {
        if (cs == null) return "";
        return cs.toString()
                .replace("״", "")
                .replace("׳", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("’", "")
                .replace("`", "")
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }


    private boolean isBeforeToday(JewishCalendar date) {
        return date.compareTo(today) < 0;
    }
    
    private void changeMonth(int monthDifference) {
        int currentYear = displayedMonth.getJewishYear();
        int currentMonth = displayedMonth.getJewishMonth();
        List<Integer> monthList = getMonthListForYear(currentYear);
        int currentIndex = monthList.indexOf(currentMonth);
        int newIndex = currentIndex + monthDifference;
        int newYear = currentYear;
        int newMonth;
        if (newIndex < 0) {
            newYear--;
            List<Integer> prevYearMonthList = getMonthListForYear(newYear);
            newMonth = prevYearMonthList.get(prevYearMonthList.size() - 1);
        } else if (newIndex >= monthList.size()) {
            newYear++;
            newMonth = JewishCalendar.TISHREI;
        } else {
            newMonth = monthList.get(newIndex);
        }
        displayedMonth.setJewishDate(newYear, newMonth, 1);
        updateCalendar();
    }
    private void changeYear(int yearDifference) {
        int newYear = displayedMonth.getJewishYear() + yearDifference;
        int currentMonth = displayedMonth.getJewishMonth();
        if (currentMonth == JewishCalendar.ADAR_II) {
            if (!new JewishCalendar(newYear, 1, 1).isJewishLeapYear()) {
                currentMonth = JewishCalendar.ADAR;
            }
        }
        displayedMonth.setJewishDate(newYear, currentMonth, 1);
        updateCalendar();
    }
    private List<Integer> getMonthListForYear(int year) {
        if (new JewishCalendar(year, 1, 1).isJewishLeapYear()) {
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
        return a.getJewishYear() == b.getJewishYear() && a.getJewishMonth() == b.getJewishMonth();
    }
}
