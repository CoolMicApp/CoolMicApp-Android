package cc.echonet.coolmicapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by stjauernick@de.loewenfelsen.net on 10/13/16.
 */

public class Utils {
    public static String getStringByName(Context context, String name) {
        int resid = context.getResources().getIdentifier(name, "string", context.getPackageName());

        if (resid == 0)
            return null;

        return context.getString(resid);
    }

    static  String getStringByName(Context context, String name, int subid) {
        int resid;

        if (subid < 0) {
            resid = context.getResources().getIdentifier(String.format(Locale.ENGLISH, "%s_n%d", name, -subid), "string", context.getPackageName());
        } else {
            resid = context.getResources().getIdentifier(String.format(Locale.ENGLISH, "%s_p%d", name, subid), "string", context.getPackageName());
        }

        if (resid == 0)
            return null;

        return context.getString(resid);
    }


    //Stolen from: http://stackoverflow.com/a/23704728 & http://stackoverflow.com/a/18879453 (removed the shortening because it did not work reliably)
    private static String generateShortUuid() {
        UUID uuid = UUID.randomUUID();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return uuid.toString();
        }


        md.update(uuid.toString().getBytes());
        byte[] digest = md.digest();

        return Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING).substring(0, 20);
    }

    static boolean checkRequiredPermissions(Context context) {
        int grantedCount = 0;

        for (String permission: Constants.REQUIRED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
            {
                grantedCount++;
            }
        }

        return grantedCount == Constants.REQUIRED_PERMISSIONS.length;
    }


    private static boolean shouldShowRequestPermissionRationale(Activity activity) {
        int grantedCount = 0;

        for (String permission: Constants.REQUIRED_PERMISSIONS) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            {
                grantedCount++;
            }
        }

        return grantedCount > 0;
    }

    static void requestPermissions(Activity activity) {
        if(!checkRequiredPermissions(activity))
        {
            if (shouldShowRequestPermissionRationale(activity)) {
                Toast.makeText(activity, R.string.settingsactivity_toast_permission_denied, Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(activity, Constants.REQUIRED_PERMISSIONS, Constants.PERMISSION_CHECK_REQUEST_CODE);
        }
        else
        {
            Toast.makeText(activity, R.string.settingsactivity_toast_permissions_granted, Toast.LENGTH_LONG).show();
        }
    }

    static boolean onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == Constants.PERMISSION_CHECK_REQUEST_CODE) {
            boolean permissions_ok = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissions_ok = false;
                    Toast.makeText(activity, activity.getString(R.string.settingsactivity_permission_not_granted, permissions[i]), Toast.LENGTH_LONG).show();
                }
            }

            if (permissions_ok) {
                Toast.makeText(activity, R.string.settingsactivity_permissions_all_granted, Toast.LENGTH_LONG).show();
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    static void loadCMTSData(Context context, String profileName) {
        SharedPreferences.Editor editor = new CoolMic(context, profileName).getPrefs().edit();

        String randomComponent = Utils.generateShortUuid();

        editor.putString("connection_address", context.getString(R.string.pref_default_connection_address));
        editor.putString("connection_username", context.getString(R.string.pref_default_connection_username));
        editor.putString("connection_password", context.getString(R.string.pref_default_connection_password));
        editor.putString("connection_mountpoint", context.getString(R.string.pref_default_connection_mountpoint, randomComponent));
        editor.putString("audio_codec", context.getString(R.string.pref_default_audio_codec));
        editor.putString("audio_samplerate", context.getString(R.string.pref_default_audio_samplerate));

        editor.apply();

        Toast.makeText(context, R.string.settings_conn_defaults_loaded, Toast.LENGTH_SHORT).show();
    }

}
