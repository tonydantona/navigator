package com.tonydantona.navigator.datatypes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// utility function for time conversions (to/from clicks)
public class TimeUtilities {
    private Date currentDate;

    public TimeUtilities() {
        super();
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public double ToClicks(Date time) {
        double clicks = 0;

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(time);
        int hr = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        clicks += (double) hr * 100;
        clicks += (double) min * 100 / 60;
        clicks += (double) sec * 100 / 3600;

        return clicks;
    }

    public Date ToDate(double clicks) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(currentDate);

        int hr = (int) clicks / 100;
        int min = (int) (clicks % 100) / 100 * 60;
        int sec = (int) ((clicks * 100) % 100) / 100 * 60;

        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hr, min, sec);

        Date date = calendar.getTime();

        return date;
    }
}
