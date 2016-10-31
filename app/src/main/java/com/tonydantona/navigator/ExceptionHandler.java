package com.tonydantona.navigator;

import android.widget.Toast;

import static android.R.id.message;

/**
 * Created by rti1ajd on 10/27/2016.
 */

public class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
//        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        String errorMsg = ex.getMessage();

    }
}
