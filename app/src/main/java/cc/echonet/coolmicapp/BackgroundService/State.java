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
    public boolean isLive = true;

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
