package com.yt.hebrewdatepicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class CustomAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // Check if the keyboard is visible by checking if the dropdown is showing.
            // This is our trigger for the FIRST back press.
            if (isPopupShowing()) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Try to hide the keyboard
                if (imm != null && imm.hideSoftInputFromWindow(getWindowToken(), 0)) {
                    // We successfully hid the keyboard.
                    // Return true to consume the event, so ONLY the keyboard closes.
                    return true;
                }
            }
        }
        // For all other cases (second back press, etc.), let the system handle it.
        return super.onKeyPreIme(keyCode, event);
    }
}