package com.alexzh.demoweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.alexzh.demoweather.data.WeatherContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utility {

    private final static String CLOUDS_WEATHER = "Clouds";
    private final static String CLEAR_WEATHER = "Clear";
    private final static String RAIN_WEATHER = "Rain";

    public static String getUnits(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric));
    }

    public static boolean isInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static String getWeatherImageUrl(String weather) {
        switch (weather) {
            case CLOUDS_WEATHER:
                return "https://ssl.gstatic.com/onebox/weather/256/cloudy.png";
            case CLEAR_WEATHER:
                return "https://ssl.gstatic.com/onebox/weather/256/sunny.png";
            case RAIN_WEATHER:
                return "https://ssl.gstatic.com/onebox/weather/256/rain.png";

        }
        return null;
    }

    static String formatTemperature(Context context, double temperature) {
        return context.getString(R.string.format_temperature, temperature);
    }

    static String formatHumidity(Context context, double humidity) {
        return context.getString(R.string.format_humidity, humidity);
    }

    static String formatPressure(Context context, double pressure) {
        return context.getString(R.string.format_pressure, pressure);
    }

    static String formatWindSpeed(Context context, double windSpeed) {
        return context.getString(R.string.format_wind_kmh, windSpeed);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    public static final String DATE_TIME_FORMAT = "yyyyMMdd HH:mm";
    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String TIME_FORMAT = "HH:mm";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        dateStr = dateStr.substring(0, 8);
        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        todayStr = todayStr.substring(0, 8);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateStr)));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                return getDayName(context, dateStr);
            } else {
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        dateStr = dateStr.substring(0, 8);
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (WeatherContract.getDbDateString(tomorrowDate).substring(0, 8).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        dateStr = dateStr.substring(0, 8);
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTime(String dateStr) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Utility.DATE_TIME_FORMAT);
        try {
            Date inputDate = dateTimeFormat.parse(dateStr);
            SimpleDateFormat timeFormat = new SimpleDateFormat(Utility.TIME_FORMAT);
            return timeFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate next day
     * @param value Some date
     * @return Next day
     */
    public static long getNextDay(long value) {
        // today
        Calendar date = new GregorianCalendar();
        int year = Integer.valueOf(String.valueOf(value).substring(0, 4));
        int month = Integer.valueOf(String.valueOf(value).substring(4,6));
        int day = Integer.valueOf(String.valueOf(value).substring(6,8));

        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month - 1);
        date.set(Calendar.DAY_OF_MONTH, day);
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        // next day
        date.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        return Long.valueOf(dateFormat.format(date.getTime()));
    }

}
