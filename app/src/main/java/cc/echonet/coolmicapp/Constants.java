package cc.echonet.coolmicapp;

import android.Manifest;

/**
 * Created by stephanj on 21.03.2016.
 */
class Constants {
    static final String ACTION_STATS_FETCH = "cc.echonet.coolmicapp.action.STATS_FETCH";
    static final String EXTRA_URL = "cc.echonet.coolmicapp.extra.STREAM_STATS_URL";

    static final String BROADCAST_STREAM_STATS_SERVICE = "cc.echonet.coolmicapp.BROADCAST_SSS";

    static final String EXTRA_DATA_STATS_OBJ = "cc.echonet.coolmicapp.extra_STREAM_STATS_OBJ";

    static final int PERMISSION_CHECK_REQUEST_CODE = 1;

    static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };


}
