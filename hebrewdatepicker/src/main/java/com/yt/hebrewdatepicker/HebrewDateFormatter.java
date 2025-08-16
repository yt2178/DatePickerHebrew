package com.yt.hebrewdatepicker;

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;

public class HebrewDateFormatter {

    // Using the official formatter from the Zmanim library is the simplest and most reliable approach.
    private static final com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter libraryFormatter =
            new com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter();

    // Static block to configure the formatter once.
    static {
        libraryFormatter.setHebrewFormat(true);
    }

    // It's simpler and more reliable to define the month names in static arrays.
    private static final String[] HEBREW_MONTHS_LEAP = {
            "תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר א'", "אדר ב'", "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול"
    };
    private static final String[] HEBREW_MONTHS_NON_LEAP = {
            "תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר", "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול"
    };

    /**
     * Formats a full date string using the library's own proven logic.
     * Example: "ל' חשוון, ה'תשפ"ז"
     */
    public static String format(JewishCalendar jc) {
        return libraryFormatter.format(jc);
    }

    /**
     * Formats just the year part for the NumberPicker. This is the simplest safe way.
     */
    public static String formatYear(int year) {
        JewishCalendar tempCal = new JewishCalendar(year, 1, 1);
        String fullDate = libraryFormatter.format(tempCal);
        String[] parts = fullDate.split(", ");
        return (parts.length > 1) ? parts[1] : String.valueOf(year);
    }

    /**
     * Formats a number to Gematria using the library's safe method.
     */
    public static String toGematria(int day) {
        return libraryFormatter.formatHebrewNumber(day);
    }

    /**
     * Returns the correct array of month names.
     */
    public static String[] getMonthNames(boolean isLeapYear) {
        return isLeapYear ? HEBREW_MONTHS_LEAP : HEBREW_MONTHS_NON_LEAP;
    }
}