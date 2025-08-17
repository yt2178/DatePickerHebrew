# Hebrew Date Picker for Android

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/yt2178/DatePickerHebrew.svg)](https://jitpack.io/#yt2178/DatePickerHebrew)

A simple and modern Hebrew Date Picker dialog for Android applications, built with Material Design principles. The library provides a calendar-style view that is intuitive and easy to integrate.

![HebrewDatePicker Screenshot](https://github.com/yt2178/DatePickerHebrew/blob/master/screenshot.png)


## Features

- **Full Hebrew Calendar:** Displays a grid-based calendar view for any Hebrew month and year.
- **Easy Navigation:** Quickly navigate between months and years.
- **Gematria Display:** All years and days are displayed in their traditional Gematria format.
- **Customizable:** Built as a `DialogFragment` for easy integration and management by the Android framework.
- **Lightweight:** Depends only on `Zmanim (KosherJava)` and AndroidX libraries.

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
    implementation("com.github.yt2178:DatePickerHebrew:1.0.1")
}
```

Sync your project with Gradle files.

## Usage

Using the `HebrewDatePickerDialog` is straightforward. Your `Activity` or `Fragment` needs to implement the `OnDateSetListener` to receive the selected date.

**1. Implement the listener:**
```java
public class MainActivity extends AppCompatActivity implements HebrewDatePickerDialog.OnDateSetListener {
    // ...
    @Override
    public void onDateSet(int year, int month, int day) {
        // A date has been selected!
        // The values are for the Hebrew calendar (e.g., year = 5785)
        JewishCalendar selectedDate = new JewishCalendar(year, month, day);

        // You can format this date for display
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);
        String formattedDate = hdf.format(selectedDate);
        
        Toast.makeText(this, "Selected Date: " + formattedDate, Toast.LENGTH_LONG).show();
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

That's it! The dialog will be displayed, and the `onDateSet` method will be called when the user confirms a date.

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
