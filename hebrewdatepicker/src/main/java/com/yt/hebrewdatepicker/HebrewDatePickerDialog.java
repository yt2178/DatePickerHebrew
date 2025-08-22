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
    private boolean disablePastDates = false;
    private boolean disableHolidaysAndShabbat = false;

    private OnDateSetListener listener;
    private JewishCalendar selectedDate;
    private JewishCalendar displayedMonth;
    private JewishCalendar today;

    private static ArrayList<String> sAllSuggestions;
    private static Map<String, JewishCalendar> sMonthMap;

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
        if (disablePastDates && isBeforeToday(selectedDate)) {
            selectedDate = (JewishCalendar) today.clone();
        }
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
           // בדוק אם החודש המוצג הוא החודש הנוכחי או בעתיד
            boolean canGoBack = isAfterCurrentMonth(displayedMonth);
            prevMonthButton.setVisibility(canGoBack ? View.VISIBLE : View.INVISIBLE);
            prevYearButton.setVisibility(canGoBack ? View.VISIBLE : View.INVISIBLE);
        } else {
            // תמיד הצג לחצנים אם תאריכי העבר אינם מושבתים
            prevMonthButton.setVisibility(View.VISIBLE);
            prevYearButton.setVisibility(View.VISIBLE);
        }
    }
    private void showYearSelectionDialog() {
        int currentYear = displayedMonth.getJewishYear();
        int startYear = disablePastDates ? today.getJewishYear() : currentYear - 50;
        int endYear = startYear + 100; // Show a 100-year range
        ArrayList<String> yearList = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            yearList.add("ה'" + HebrewDateFormatter.toGematria(i % 1000));
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("בחר שנה")
                .setItems(yearList.toArray(new String[0]), (d, which) -> {
                    int selectedYear = startYear + which;
                   // אם תעבור לשנה הנוכחית מהעתיד, אל תקפוץ לחודש האחרון
                    if (disablePastDates && selectedYear == today.getJewishYear() && displayedMonth.getJewishMonth() < today.getJewishMonth()) {
                        displayedMonth.setJewishMonth(today.getJewishMonth());
                    }
                    displayedMonth.setJewishYear(selectedYear);
                    updateCalendar();
                })
                .show();
    }
    /**
     * מציג דיאלוג לבחירה מהירה של חודש מתוך השנה המוצגת.
     * הפונקציה מסננת באופן דינמי חודשים מהעבר אם האפשרות {@code disablePastDates} מופעלת
     * והמשתמש צופה בשנה הנוכחית.
     */
    private void showMonthSelectionDialog() {
        int currentYear = displayedMonth.getJewishYear();
        // --- שלב 1: הכנת רשימת החודשים לתצוגה ---
        List<Integer> allMonthsInYear = getMonthListForYear(currentYear);
        ArrayList<String> monthNamesToDisplay = new ArrayList<>();

        com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter hdf = new com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter();
        hdf.setHebrewFormat(true);

        if (disablePastDates && currentYear == today.getJewishYear()) {
            // אם אסור להציג עבר ואנחנו בשנה הנוכחית:
            // התחל מהחודש הנוכחי ורוץ עד סוף השנה.
            int currentMonthIndex = allMonthsInYear.indexOf(today.getJewishMonth());
            if (currentMonthIndex != -1) {
                for (int i = currentMonthIndex; i < allMonthsInYear.size(); i++) {
                    int monthConst = allMonthsInYear.get(i);
                    monthNamesToDisplay.add(hdf.formatMonth(new JewishCalendar(currentYear, monthConst, 1)));
                }
            }
        } else {
            // אם מותר להציג עבר, או שאנחנו לא בשנה הנוכחית:
            // הצג את כל חודשי השנה ללא סינון.
            for (int monthConst : allMonthsInYear) {
                monthNamesToDisplay.add(hdf.formatMonth(new JewishCalendar(currentYear, monthConst, 1)));
            }
        }

        // --- שלב 2: הצגת הדיאלוג ובחירה ---
        new AlertDialog.Builder(requireContext())
                .setTitle("בחר חודש")
                .setItems(monthNamesToDisplay.toArray(new String[0]), (d, which) -> {
                    // 'which' הוא המיקום ברשימה (המסוננת או המלאה) שהצגנו.
                    // אנחנו צריכים למצוא את מספר החודש האמיתי שתואם לבחירה הזו.
                    String selectedMonthName = monthNamesToDisplay.get(which);

                    for (int monthConst : allMonthsInYear) {
                        String name = hdf.formatMonth(new JewishCalendar(currentYear, monthConst, 1));
                        if (name.equals(selectedMonthName)) {
                            displayedMonth.setJewishMonth(monthConst);
                            updateCalendar();
                            break; // מצאנו, אין צורך להמשיך
                        }
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
        ensureSuggestionsAreReady();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_jump_to_date, null);
        CustomAutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.actv_month_year);
        TextView tvInstructions = dialogView.findViewById(R.id.tv_instructions);
        TextView tvError = dialogView.findViewById(R.id.tv_error_message);

        ArrayList<String> displaySuggestions = new ArrayList<>();
        if (disablePastDates) {
            for (String suggestion : sAllSuggestions) {
                JewishCalendar cal = sMonthMap.get(suggestion);
                if (cal != null && !isBeforeCurrentMonth(cal)) {
                    displaySuggestions.add(suggestion);
                }
            }
        } else {
            displaySuggestions.addAll(sAllSuggestions);
        }

        ArrayAdapter<String> adapter = createSuggestionsAdapter(displaySuggestions);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvError.setVisibility(View.GONE);
                tvInstructions.setText("הקלד חודש ושנה (למשל: אלול תשפד)");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        builder.setView(dialogView)
                .setTitle("קפיצה לחודש ושנה")
                .setPositiveButton("קפוץ", null)
                .setNegativeButton("ביטול", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            negativeButton.setOnClickListener(v -> dialog.dismiss());

            positiveButton.setOnClickListener(v -> {
                String input = autoCompleteTextView.getText().toString().trim();
                if (input.isEmpty()) {
                    tvError.setText("השדה לא יכול להיות ריק");
                    tvError.setVisibility(View.VISIBLE);
                    tvInstructions.setVisibility(View.GONE);
                    return;
                }

                String normalizedInput = normalizeHebrew(input);
                JewishCalendar jumpToDate = null;

                for (String key : sMonthMap.keySet()) {
                    if (normalizeHebrew(key).contains(normalizedInput)) {
                        jumpToDate = sMonthMap.get(key);
                        break;
                    }
                }

                if (jumpToDate != null) {
                    String[] inputParts = input.trim().split("\\s+");
                    if (inputParts.length == 1 && sMonthMap.get(input) == null) {
                        int targetYear = jumpToDate.getJewishYear();

                        // --- THIS IS THE CRITICAL FIX ---
                        List<String> monthsOfYear = new ArrayList<>();
                        Map<String, JewishCalendar> tempMap = new HashMap<>();
                        List<Integer> allMonthConstantsInYear = getMonthListForYear(targetYear);

                        for (int monthConst : allMonthConstantsInYear) {
                            // Apply the same filter here!
                            if (disablePastDates && targetYear == today.getJewishYear() && monthConst < today.getJewishMonth()) {
                                continue; // Skip past months
                            }
                            // Create the full month name string for this valid month
                            JewishCalendar tempCal = new JewishCalendar(targetYear, monthConst, 1);
                            String monthName = HebrewDateFormatter.formatMonth(tempCal);
                            String yearText = HebrewDateFormatter.toGematria(targetYear % 1000);
                            String fullKey = monthName + " " + yearText;

                            monthsOfYear.add(fullKey);
                            tempMap.put(fullKey, tempCal);
                        }
                        // --- END OF FIX ---

                        Map<String, Integer> monthOrderMap = new HashMap<>();
                        monthOrderMap.put("תשרי", 1);
                        monthOrderMap.put("מר חשוון", 2);
                        monthOrderMap.put("חשוון", 2);
                        monthOrderMap.put("כסלו", 3);
                        monthOrderMap.put("טבת", 4);
                        monthOrderMap.put("שבט", 5);
                        monthOrderMap.put("אדר", 6);
                        monthOrderMap.put("אדר א׳", 6);
                        monthOrderMap.put("אדר ב׳", 7);
                        monthOrderMap.put("ניסן", 8);
                        monthOrderMap.put("אייר", 9);
                        monthOrderMap.put("סיון", 10);
                        monthOrderMap.put("תמוז", 11);
                        monthOrderMap.put("אב", 12);
                        monthOrderMap.put("אלול", 13);

                        Collections.sort(monthsOfYear, (a, b) -> {
                            Integer orderA = getMonthOrder(a, monthOrderMap);
                            Integer orderB = getMonthOrder(b, monthOrderMap);
                            if (orderA == null || orderB == null) return 0;
                            return orderA.compareTo(orderB);
                        });

                        AlertDialog.Builder monthBuilder = new AlertDialog.Builder(requireContext());
                        monthBuilder.setTitle("בחר חודש לשנה " + input)
                                .setItems(monthsOfYear.toArray(new String[0]), (dialog1, whichMonth) -> {
                                    JewishCalendar selectedMonth = tempMap.get(monthsOfYear.get(whichMonth));
                                    if (selectedMonth != null) {
                                        displayedMonth.setJewishDate(selectedMonth.getJewishYear(), selectedMonth.getJewishMonth(), 1);
                                        selectedDate.setJewishDate(selectedMonth.getJewishYear(), selectedMonth.getJewishMonth(), 1);
                                        updateCalendar();
                                    }
                                })
                                .show();
                        dialog.dismiss();
                        return;
                    }

                    displayedMonth.setJewishDate(jumpToDate.getJewishYear(), jumpToDate.getJewishMonth(), 1);
                    selectedDate.setJewishDate(jumpToDate.getJewishYear(), jumpToDate.getJewishMonth(), 1);
                    updateCalendar();
                    dialog.dismiss();
                } else {
                    tvError.setText("אנא הזן תאריך חוקי מהרשימה או שנה בגימטריה");
                    tvError.setVisibility(View.VISIBLE);
                    tvInstructions.setText("הקלד את החודש והשנה (למשל: תשרי תשפ''ד). ניתן גם להזין רק את השנה, והמערכת תציע את כל החודשים שלה.");
                    tvInstructions.setVisibility(View.VISIBLE);
                }
            });
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        dialog.show();

        autoCompleteTextView.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }
    /**
    * פונקציית עוזר כדי לקבל בחוזקה את סדר המיון למחרוזת של חודש.
     * זה מטפל בשמות חודש רב-מילים כמו "Aber ucouf".
     * @param monthString מחרוזת החודש המלאה (e.g., "מר חשוון תשפ"ח").
     * @param orderMap מפת שמות החודש לסדר המיון שלהם.
     * @return סדר המיון שלם, או null אם לא נמצא.
     */
    private Integer getMonthOrder(String monthString, Map<String, Integer> orderMap) {
        // Iterate through all known month names in the map
        for (String monthKey : orderMap.keySet()) {
            // Check if the full string starts with a known month name
            if (monthString.startsWith(monthKey)) {
                return orderMap.get(monthKey);
            }
        }
        return null; // Month not found
    }
    /**
     * פונקציה נפרדת לנרמול טקסט עברי להשוואה.
     */
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
    /**
     * יוצר ושומר במטמון (cache) את רשימת ההצעות אם היא עדיין לא קיימת.
     * התהליך רץ רק פעם אחת כדי לשפר ביצועים.
     */
    private void ensureSuggestionsAreReady() {
        if (sAllSuggestions != null && sMonthMap != null) return;
        sAllSuggestions = new ArrayList<>();
        sMonthMap = new HashMap<>();
        JewishCalendar cal = new JewishCalendar();
        cal.setJewishDayOfMonth(1);
        cal.setJewishYear(cal.getJewishYear() - 25);
        int monthsToShow = 51 * 13;
        for (int i = 0; i < monthsToShow; i++) {
            String yearText = HebrewDateFormatter.toGematria(cal.getJewishYear() % 1000);
            String suggestionText = HebrewDateFormatter.formatMonth(cal) + " " + yearText;
            if (!sMonthMap.containsKey(suggestionText)) {
                sAllSuggestions.add(suggestionText);
                sMonthMap.put(suggestionText, (JewishCalendar) cal.clone());
            }
            int currentYear = cal.getJewishYear();
            int currentMonth = cal.getJewishMonth();
            List<Integer> monthList = getMonthListForYear(currentYear);
            int currentIndex = monthList.indexOf(currentMonth);
            if (currentIndex == -1) break;
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
    }
    /**
     * יוצר וקובע את התצורה של ה-ArrayAdapter עם פילטר מותאם אישית להצעות.
     */
    private ArrayAdapter<String> createSuggestionsAdapter(List<String> suggestions) {
        return new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>(suggestions)) {
            private final List<String> allItems = new ArrayList<>(suggestions);
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> filteredList = new ArrayList<>();
                        if (constraint == null || constraint.length() == 0) {
                            filteredList.addAll(allItems);
                        } else {
                            String filterPattern = normalizeHebrew(constraint);
                            for (String item : allItems) {
                                if (normalizeHebrew(item).contains(filterPattern)) {
                                    filteredList.add(item);
                                }
                            }
                        }
                        results.values = filteredList;
                        results.count = filteredList.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
                        clear();
                        if (results.values != null) {
                            //noinspection unchecked
                            addAll((List<String>) results.values);
                        }
                        notifyDataSetChanged();
                    }
                };
            }
        };
    }
    private boolean isBeforeToday(JewishCalendar date) { return date.compareTo(today) < 0; }
    private boolean isSameMonth(JewishCalendar a, JewishCalendar b) {
        if (a == null || b == null) return false;
        return a.getJewishYear() == b.getJewishYear() && a.getJewishMonth() == b.getJewishMonth();
    }
    private boolean isAfterCurrentMonth(JewishCalendar date) {
        JewishCalendar firstDayOfDate = (JewishCalendar) date.clone();
        firstDayOfDate.setJewishDayOfMonth(1);
        JewishCalendar firstDayOfToday = (JewishCalendar) today.clone();
        firstDayOfToday.setJewishDayOfMonth(1);
        return firstDayOfDate.compareTo(firstDayOfToday) > 0;
    }
    private boolean isBeforeCurrentMonth(JewishCalendar date) {
        JewishCalendar todayInFirstDayOfMonth = (JewishCalendar) today.clone();
        todayInFirstDayOfMonth.setJewishDayOfMonth(1);
        JewishCalendar dateInFirstDayOfMonth = (JewishCalendar) date.clone();
        dateInFirstDayOfMonth.setJewishDayOfMonth(1);
        return dateInFirstDayOfMonth.compareTo(todayInFirstDayOfMonth) < 0;
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
}
