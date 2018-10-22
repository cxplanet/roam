package mobile.pamdas.org.roam;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class Utils {

    private static final SimpleDateFormat isoUtcFormatter;
    private static final String PREF_UNIQUE_ID = "deviceUUID";
    private static final String PREF_DEVICE_NAME = "deviceName";
    private static String deviceId;


    public static String getUTCString(Date date) {
        return isoUtcFormatter.format(date);
    }

    // an alternate means of getting a unique ID. We could ask for access
    // to read the IMEI, but I think that's a bit too intrusive for now,
    // maybe not in a real world deployment (when you are worried about bad actors)
    public synchronized static String deviceId(Context context) {
        if (deviceId == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            deviceId = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, deviceId);
                editor.commit();
            }
        }
        return deviceId;
    }

    public synchronized static void setDeviceName(String name, Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_DEVICE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_DEVICE_NAME, name);
        editor.commit();
    }

    public synchronized static String getDeviceName(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_DEVICE_NAME, Context.MODE_PRIVATE);
        String deviceName = sharedPrefs.getString(PREF_DEVICE_NAME, null);
        return deviceName;
    }

    static {
        isoUtcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
        isoUtcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
