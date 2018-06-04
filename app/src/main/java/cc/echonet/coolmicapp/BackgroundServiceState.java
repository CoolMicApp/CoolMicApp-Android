package cc.echonet.coolmicapp;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;

import cc.echonet.coolmicdspjava.WrapperConstants;

/**
 * Created by stephan on 2/24/18.
 */

class BackgroundServiceState implements Serializable {
    Constants.CONTROL_UI oldState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
    Constants.CONTROL_UI uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
    String txtState = "disconnected";

    int bindCounts = 0;
    int clientCount = 0;
    boolean initialConnectPerformed = false;
    boolean hasCore = false;
    WrapperConstants.WrapperInitializationStatus wrapperInitializationStatus;
    long timerInMS = 0L;
    long startTime = 0L;
    String timerString = "00:00:00";
    String listenersString = "0 (Max: 0 )";
    long lastStateFetch = 0L;
    boolean hadError = false;

    BackgroundServiceState() {
        Log.v("BackgroundServiceState", "BSS constructed");
    }

}
