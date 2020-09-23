/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2020
 *       on behalf of Jordan Erickson.
 *
 * This file is part of Cool Mic.
 *
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cc.echonet.coolmicapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;

import cc.echonet.coolmicapp.Configuration.DialogIdentifier;
import cc.echonet.coolmicapp.Configuration.Profile;

/**
 * Created by stjauernick@de.loewenfelsen.net on 10/13/16.
 */

public final class Utils {
    private static final int PERMISSION_CHECK_REQUEST_CODE = 1;
    private static final int PERMISSION_CHECK_PASSIVE_REQUEST_CODE = 2;

    public static @Nullable String getStringByName(Context context, String name) {
        int resid = context.getResources().getIdentifier(name, "string", context.getPackageName());

        if (resid == 0)
            return null;

        return context.getString(resid);
    }

    public static @Nullable String getStringByName(Context context, String name, int subid) {
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

    private static @NotNull String[] getRequiredPermissionList(@NotNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkRequiredPermissions(@NotNull Context context, boolean minimalOnly) {
        final String[] requiredPermissions = getRequiredPermissionList(context);
        int count = 0;
        int grantedCount = 0;

        for (String permission : requiredPermissions) {
            if (minimalOnly && permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                continue;

            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                grantedCount++;
            }
            count++;
        }

        return grantedCount == count;
    }


    private static boolean shouldShowRequestPermissionRationale(@NotNull Activity activity) {
        final String[] requiredPermissions = getRequiredPermissionList(activity);
        int grantedCount = 0;

        for (String permission : requiredPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                grantedCount++;
            }
        }

        return grantedCount > 0;
    }

    static void requestPermissions(@NotNull Activity activity, @NotNull Profile profile, boolean passive) {
        if (!checkRequiredPermissions(activity, false)) {
            new Dialog(DialogIdentifier.PERMISSIONS, activity, profile,
                    () -> ActivityCompat.requestPermissions(activity, getRequiredPermissionList(activity), passive ? PERMISSION_CHECK_PASSIVE_REQUEST_CODE : PERMISSION_CHECK_REQUEST_CODE)
            ).show();
        } else {
            Toast.makeText(activity, R.string.settingsactivity_toast_permissions_granted, Toast.LENGTH_LONG).show();
        }
    }

    static boolean onRequestPermissionsResult(@NotNull Context context, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CHECK_REQUEST_CODE || requestCode == PERMISSION_CHECK_PASSIVE_REQUEST_CODE) {
            boolean permissions_ok = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissions_ok = false;
                    Toast.makeText(context, context.getString(R.string.settingsactivity_permission_not_granted, permissions[i]), Toast.LENGTH_LONG).show();
                }
            }

            if (permissions_ok) {
                Toast.makeText(context, R.string.settingsactivity_permissions_all_granted, Toast.LENGTH_LONG).show();
            }

            return requestCode == PERMISSION_CHECK_REQUEST_CODE;
        } else {
            return false;
        }
    }

    static void loadCMTSData(@NotNull Context context, @NotNull Profile profile) {
        CMTS.loadCMTSData(profile);

        Toast.makeText(context, R.string.settings_conn_defaults_loaded, Toast.LENGTH_SHORT).show();
    }

    static AlertDialog.Builder buildAlertDialogCMTSTOS(@NotNull Context context) {
        AlertDialog.Builder alertDialogCMTSTOS = new AlertDialog.Builder(context);
        alertDialogCMTSTOS.setTitle(R.string.coolmic_tos_title);
        alertDialogCMTSTOS.setMessage(R.string.coolmic_tos);
        alertDialogCMTSTOS.setNegativeButton(R.string.coolmic_tos_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return alertDialogCMTSTOS;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }



    static int normalizeVUMeterPower(double power) {
        int g_p = (int) ((60. + power) * (100. / 60.));

        if (g_p > 100) {
            g_p = 100;
        }

        if (g_p < 0) {
            g_p = 0;
        }

        return g_p;
    }

    static String VUMeterPeakToString(int peak) {
        if (peak == -32768 || peak == 32767) {
            return "P";
        } else if (peak < -30000 || peak > 30000) {
            return "p";
        } else if (peak < -8000 || peak > 8000) {
            return "g";
        } else {
            return "";
        }
    }

    @NonNull
    static String VUMeterPowerToString(double power) {
        if (power < -100) {
            return "-100";
        } else if (power > 0) {
            return "0";
        } else {
            return String.format(Locale.ENGLISH, "%.2f", power);
        }
    }


    /**
     * Show soft keyboard, Dialog uses
     *
     * @param activity Current Activity
     */
    static void showSoftInput(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
