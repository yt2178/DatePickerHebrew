package com.yt.hebrew;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.yt.hebrewdatepicker.HebrewDatePickerDialog;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.util.GeoLocation;

public class MainActivity extends AppCompatActivity implements HebrewDatePickerDialog.OnDateSetListener {
    private final Handler mHandler = new Handler();
    private TextView textViewTime, textViewHebrewDate, textViewParsha, textViewSunrise, textViewSunset, textViewSelectedDate;
    private Button buttonShowDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Existing TextViews
        textViewTime = findViewById(R.id.textViewTime);
        textViewHebrewDate = findViewById(R.id.textViewHebrewDate);
        textViewParsha = findViewById(R.id.textViewParsha);
        textViewSunrise = findViewById(R.id.textViewSunrise);
        textViewSunset = findViewById(R.id.textViewSunset);

        // New Views
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonShowDatePicker = findViewById(R.id.btn_open_picker);

        buttonShowDatePicker.setOnClickListener(v -> {
            HebrewDatePickerDialog dialog = new HebrewDatePickerDialog();
            dialog.setOnDateSetListener(this); // 'this' refers to MainActivity which implements the listener
            dialog.show(getSupportFragmentManager(), "HebrewDatePickerDialog");
        });

        updateZmanim();
    }

    private void updateZmanim() {
        final Runnable runnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                GeoLocation location = new GeoLocation("Tel Aviv", 32.0853, 34.7818, 0, TimeZone.getTimeZone("Asia/Jerusalem"));
                ZmanimUtil zmanimUtil = new ZmanimUtil(location);

                Date now = new Date();
                String gregDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(now);

                String hebDate = JewishDateUtil.getHebrewDate();
                String parsha = JewishDateUtil.getParsha();
                String sunrise = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(zmanimUtil.getSunrise());
                String sunset = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(zmanimUtil.getSunset());

                textViewTime.setText("עכשיו: " + gregDate);
                textViewHebrewDate.setText("תאריך עברי: " + hebDate);
                textViewParsha.setText("פרשת השבוע: " + parsha);
                textViewSunrise.setText("זריחה: " + sunrise);
                textViewSunset.setText("שקיעה: " + sunset);

                mHandler.postDelayed(this, 1000);

        }

    };

            mHandler.post(runnable); // הפעלת ה-Runnable לראשונה

}

    // This method is called when a date is selected in the dialog
    @Override
    public void onDateSet(int year, int month, int day) {
        // Create a JewishCalendar object from the selected date
        JewishCalendar selectedJewishCalendar = new JewishCalendar(year, month, day);

        // Format it into a readable Hebrew string
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);
        String selectedDateStr = hdf.format(selectedJewishCalendar);

        // Set the string to the new TextView
        textViewSelectedDate.setText("נבחר: " + selectedDateStr);
    }
}