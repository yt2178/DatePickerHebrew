package com.yt.hebrew;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import com.yt.hebrewdatepicker.HebrewDatePickerDialog;

public class MainActivity extends AppCompatActivity implements HebrewDatePickerDialog.OnDateSetListener {

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
            dialog.setOnDateSetListener(this); // 'זה' מתייחס למצב ראשי שמיישם את המאזין
            dialog.show(getSupportFragmentManager(), "HebrewDatePickerDialog");
        });

        updateZmanim();
    }

    private void updateZmanim() {
        GeoLocation location = new GeoLocation("Tel Aviv", 32.0853, 34.7818, 0, TimeZone.getTimeZone("Asia/Jerusalem"));
        ZmanimUtil zmanimUtil = new ZmanimUtil(location);

        Date now = new Date();
        String gregDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now);
        String hebDate = JewishDateUtil.getHebrewDate();
        String parsha = JewishDateUtil.getParsha();
        String sunrise = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(zmanimUtil.getSunrise());
        String sunset = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(zmanimUtil.getSunset());

        textViewTime.setText("עכשיו: " + gregDate);
        textViewHebrewDate.setText("תאריך עברי: " + hebDate);
        textViewParsha.setText("פרשת השבוע: " + parsha);
        textViewSunrise.setText("זריחה: " + sunrise);
        textViewSunset.setText("שקיעה: " + sunset);
    }

    @Override
    public void onDateSet(JewishCalendar selectedDate, String formattedDate) {
        textViewSelectedDate.setText("נבחר: " + formattedDate);
    }
}