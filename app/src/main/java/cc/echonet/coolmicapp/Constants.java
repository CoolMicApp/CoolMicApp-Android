package cc.echonet.coolmicapp;

import android.Manifest;

/**
 * Created by stephanj on 21.03.2016.
 */
class Constants {
    static final int PERMISSION_CHECK_REQUEST_CODE = 1;

    static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
}
