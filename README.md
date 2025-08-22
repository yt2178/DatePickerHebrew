# Hebrew Date Picker for Android

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/yt2178/DatePickerHebrew.svg)](https://jitpack.io/#yt2178/DatePickerHebrew)

A simple, modern, and highly customizable Hebrew Date Picker dialog for Android applications. Built with Material Design principles, this library provides an intuitive calendar-style view for Hebrew date selection.

![HebrewDatePicker Screenshot](https://github.com/user-attachments/assets/7c99aeed-c33a-4113-8f88-9d65d6bbd6bc)![HebrewDatePicker Screenshot](https://github.com/yt2178/DatePickerHebrew/blob/master/screenshot.png)

### **updated 👇 1.0.4**

<img width="480" height="854" alt="image" src="https://github.com/user-attachments/assets/72903b54-5ef7-4b68-92e9-931a0d95fb94" />




## Features

-   **Full Hebrew Calendar:** Displays a grid-based calendar view for any Hebrew month and year.
-   **Intuitive Navigation:**
    -   Quickly navigate between months and years with arrow buttons.
    -   Click on the month or year title to open a selection list for rapid navigation.
    -   Use the "Jump to Date" feature with predictive text to find any date in a 50-year range.
-   **Highly Customizable:**
    -   Disable selection of past dates.
    -   Disable selection of Shabbat and Jewish holidays/fasts.
-   **Gematria Display:** All years and days are displayed in their traditional Gematria format.
-   **Easy Integration:** Built as a `DialogFragment` and distributed via JitPack for simple, plug-and-play usage.
-   **Lightweight:** Depends only on `Zmanim (KosherJava)` and standard AndroidX libraries.



## Installation

This library is available on JitPack. To add it to your project, follow these steps:

**Step 1. Add the JitPack repository**

Add the JitPack repository to your root `settings.gradle.kts` (or `settings.gradle`) file:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2. Add the dependency**

Add the dependency to your app's `build.gradle.kts` (or `build.gradle`) file. 

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.yt2178:DatePickerHebrew:1.0.4")
}
```

Sync your project with Gradle files.

## Usage

Using the `HebrewDatePickerDialog` is straightforward. Your `Activity` or `Fragment` needs to implement the `OnDateSetListener` to receive the selected date.

**1. Implement the listener:**
```java
public class MainActivity extends AppCompatActivity implements HebrewDatePickerDialog.OnDateSetListener {
    
    private TextView mySelectedDateTextView; // Example TextView
    
    // ... inside onCreate, find your TextView
    // mySelectedDateTextView = findViewById(R.id.your_textview_id);

    @Override
    public void onDateSet(JewishCalendar selectedDate, String formattedDate) {
        // A date has been selected!
        // The dialog returns both the formatted string and the JewishCalendar object.
        
        // Display the result directly in your TextView
        mySelectedDateTextView.setText(formattedDate);
    }
}
```

**2. Show the dialog:**
```java
// Inside a button's OnClickListener or any other event
button.setOnClickListener(v -> {
    HebrewDatePickerDialog dialog = new HebrewDatePickerDialog();
    dialog.setOnDateSetListener(this); // 'this' refers to your Activity/Fragment
    dialog.show(getSupportFragmentManager(), "HebrewDatePickerDialog");
});
```
## NEW NEW NEW!!!
Customization (Advanced Usage)
You can chain methods on the Builder to customize the dialog's behavior.
Example: Disable past dates and holidays
This is useful for booking or scheduling applications where users should only select future, available dates.
```Java
button.setOnClickListener(v -> {
    new HebrewDatePickerDialog.Builder()
        .setDisablePastDates(true)
        .setDisableHolidaysAndShabbat(true)
        .setOnDateSetListener(this)
        .build()
        .show(getSupportFragmentManager(), "HebrewDatePickerDialog");
});
```
When `setDisablePastDates(true)` is used:
-   Past days in the calendar will be grayed out and unselectable.
-   The back-navigation buttons will be hidden when viewing the current month.
-   The month, year, and "Jump to Date" selection lists will only show future options.


When setDisableHolidaysAndShabbat(true) is used:
-   All Shabbatot, Jewish holidays (Yom Tov), and fast days (Taanit) will be grayed out and unselectable.

---
## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
Copyright (c) 2025 "The Creator YT"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
