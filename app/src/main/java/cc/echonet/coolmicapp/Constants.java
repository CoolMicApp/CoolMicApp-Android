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

    static final int NOTIFICATION_ID_LED = 0;

    static final int PERMISSION_CHECK_REQUEST_CODE = 1;

    static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    };

    static final int C2S_MSG_SAY_HELLO = 1;
    static final int C2S_MSG_REGISTER = 2;
    static final int C2S_MSG_DEREGISTER = 3;
    static final int C2S_MSG_STATE = 4;
    static final int C2S_MSG_STREAM_STOP = 5;
    static final int C2S_MSG_STREAM_START = 6;
    static final int S2C_MSG_PONG = 51;
    static final int S2C_MSG_STATE_REPLY = 52;
    static final int S2C_MSG_STREAM_STOP_REPLY = 53;
    static final int S2C_MSG_STREAM_START_REPLY = 54;
    static final int S2C_MSG_VUMETER = 55;


    enum CONTROL_UI {
        CONTROL_UI_CONNECTING,
        CONTROL_UI_CONNECTED,
        CONTROL_UI_RECONNECTING,
        CONTROL_UI_RECONNECTED,
        CONTROL_UI_DISCONNECTED
    }
}
