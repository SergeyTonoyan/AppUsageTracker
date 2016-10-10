package net.abraksis.appusagetracker;

import java.util.Calendar;

public class IntervalManager {

    public static Calendar getIntervalFirstDate(Interval interval, Calendar secondDate) {

        Calendar firstDate = (Calendar) secondDate.clone();

        if (interval == Interval.DAY) {

            firstDate.set(Calendar.HOUR_OF_DAY, 0);
            firstDate.set(Calendar.MINUTE, 0);
            firstDate.set(Calendar.SECOND, 0);
        }

        if (interval == Interval.WEEK) {

            firstDate.add(Calendar.DAY_OF_MONTH, -7);
        }

        if (interval == Interval.MONTH) {

            firstDate.set(Calendar.DAY_OF_MONTH, 1);
        }
        return firstDate;
    }
}
