package cc.echonet.coolmicapp.BackgroundService;

import android.util.Log;

import java.io.Serializable;

import cc.echonet.coolmicdspjava.WrapperConstants;

/**
 * Created by stephan on 2/24/18.
 */

public class State implements Serializable {
    public Constants.CONTROL_UI oldState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
    public Constants.CONTROL_UI uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
    public String txtState = "Disconnected";

    public int bindCounts = 0;
    public int clientCount = 0;
    public boolean initialConnectPerformed = false;
    public boolean hasCore = false;
    public WrapperConstants.WrapperInitializationStatus wrapperInitializationStatus;
    public long timerInMS = 0L;
    public long startTime = 0L;
    public String timerString = "00:00:00";
    public String listenersString = "0 (Max: 0 )";
    public long lastStateFetch = 0L;
    public boolean hadError = false;
    public long channels = 1;

    public State() {
        Log.v("State", "BSS constructed");
    }
}
