package cc.echonet.coolmicapp.BackgroundService;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;

import cc.echonet.coolmicapp.R;
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
    public int listeners_current;
    public int listeners_peak;
    public long lastStateFetch = 0L;
    public boolean hadError = false;
    public long channels = 1;

    public State() {
        Log.v("State", "BSS constructed");
    }

    public String getTimerString(Context context) {
        int secs = (int) (timerInMS / 1000);
        int mins = secs / 60;
        int hours = mins / 60;

        secs = secs % 60;
        mins = mins % 60;

        return context.getString(R.string.timer_format, hours, mins, secs);
    }

    public String getTextState(Context context) {
        return txtState;
    }

    public String getListenersString(Context context) {
        return context.getString(R.string.formatListeners, listeners_current, listeners_peak);
    }
}
