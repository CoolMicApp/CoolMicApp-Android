package cc.echonet.coolmicapp;

import android.util.Log;

import java.io.Serializable;

import cc.echonet.coolmicdspjava.WrapperConstants;

/**
 * Created by stephan on 2/24/18.
 */

class BackgroundServiceState implements Serializable {
    Constants.CONTROL_UI uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
    String txtState = "N/A";

    static int serviceRestarts = -1;
    boolean initialConnectPerformed = false;
    boolean hasCore = false;
    WrapperConstants.WrapperInitializationStatus wrapperInitializationStatus;
    long timerInMS = 0L;
    long startTime = 0L;
    long lastStateFetch = 0L;

    BackgroundServiceState() {
        Log.v("BackgroundServiceState", "BSS constructed");
    }

}
