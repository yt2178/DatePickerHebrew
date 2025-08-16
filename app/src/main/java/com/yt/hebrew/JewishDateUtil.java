package com.yt.hebrew;

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;

public class JewishDateUtil {

    public static JewishCalendar getJewishCalendar() {
        return new JewishCalendar(); // לועזי לעברי, משתמש בזמן המערכת
    }

    public static String getHebrewDate() {
        JewishCalendar jc = getJewishCalendar();
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);
        return hdf.format(jc); // תאריך עברי
    }

    public static String getParsha() {
        JewishCalendar jc = getJewishCalendar();
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);
        return hdf.formatParsha(jc);
    }
}
