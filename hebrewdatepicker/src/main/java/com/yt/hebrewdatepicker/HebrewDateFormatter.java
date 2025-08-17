package com.yt.hebrewdatepicker;

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;

/**
 * A simplified utility class that acts as a wrapper around the
 * com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter.
 * It delegates all formatting logic to the underlying library for accuracy and simplicity.
 */
public class HebrewDateFormatter {

    // A single, static instance of the library's formatter.
    private static final com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter libraryFormatter;

    // Static block to configure the formatter once upon class loading.
    static {
        libraryFormatter = new com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter();
        libraryFormatter.setHebrewFormat(true); // Ensure output is in Hebrew.
    }

    /**
     * Formats a full Hebrew date using the library's built-in logic.
     * Example: "ל' חשוון, ה'תשפ"ז"
     * @param jc The JewishCalendar instance to format.
     * @return A formatted date string.
     */
    public static String format(JewishCalendar jc) {
        return libraryFormatter.format(jc);
    }

    /**
     * Formats the name of the month from a JewishCalendar instance.
     * @param jc The JewishCalendar instance.
     * @return The Hebrew name of the month (e.g., "אדר א'").
     */
    public static String formatMonth(JewishCalendar jc) {
        return libraryFormatter.formatMonth(jc);
    }

    /**
     * Formats a number into a Hebrew Gematria string (e.g., 30 -> "ל'").
     * @param number The number to convert.
     * @return The Gematria representation as a String.
     */
    public static String toGematria(int number) {
        return libraryFormatter.formatHebrewNumber(number);
    }
}