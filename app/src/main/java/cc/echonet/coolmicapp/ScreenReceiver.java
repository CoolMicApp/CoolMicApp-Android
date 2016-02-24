/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
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
 */
package cc.echonet.coolmicapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    private static final int LED_NOTIFICATION_ID = 0;
    private boolean ThreadStatus = false;
    private NotificationManager NotificationManager;

    public void setThreadStatus(boolean status) {
        this.ThreadStatus = status;
    }

    public void setNT(NotificationManager nf) {
        this.NotificationManager = nf;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.v("$$$$$$", "In Method:  ACTION_SCREEN_OFF");
            //     Toast.makeText(context, "Intent ACTION_SCREEN_OFF.", Toast.LENGTH_LONG).show();
            // onPause() will be called.
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // Toast.makeText(context, "Intent ACTION_SCREEN_ON.", Toast.LENGTH_LONG).show();
            Log.v("$$$$$$", "In Method:  ACTION_SCREEN_ON");
            //onResume() will be called.
            //Better check for whether the screen was already locked
            //if locked, do not take any resuming action in onResume()
            //Suggest you, not to take any resuming action here.
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            //  Toast.makeText(context, "Intent ACTION_USER_PRESENT.", Toast.LENGTH_LONG).show();
            Log.v("$$$$$$", "In Method:  ACTION_USER_PRESENT");
            //Handle resuming events
        }

    }

}
    
