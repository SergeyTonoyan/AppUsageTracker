package net.abraksis.appusagetracker;


public class TimeHelper {

    public static int getDay(int timeMilliSeconds) {
        return (timeMilliSeconds/ (1000 * 60 * 60 * 24));
    }

    public static int getHour(int timeMilliSeconds) {
        return (timeMilliSeconds / (1000 * 60 * 60)) % 24;
    }

    public static int getMinutes(int timeMilliSeconds) {
        return (timeMilliSeconds / (1000 * 60)) % 60;
    }

    public static int getSeconds(int timeMilliSeconds) {
        return (timeMilliSeconds / 1000) % 60;
    }
}
