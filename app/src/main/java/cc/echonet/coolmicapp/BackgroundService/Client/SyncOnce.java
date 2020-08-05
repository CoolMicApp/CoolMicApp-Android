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

package cc.echonet.coolmicapp.BackgroundService.Client;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class SyncOnce {
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String TAG = "BGS/Client/SyncOnce";

    private final @NotNull Object notifyBus = new Object();
    private boolean state = false;

    public void ready() {
        state = true;

        Log.d(TAG, "ready: this=" + this);

        synchronized (notifyBus) {
            notifyBus.notifyAll();
        }
    }

    public void sync() throws InterruptedException {
        Log.d(TAG, "sync: this=" + this + ", IN");

        while (true) {
            synchronized (this) {
                if (state) {
                    Log.d(TAG, "sync: this=" + this + ", OUT");
                    return;
                }
            }

            synchronized (notifyBus) {
                notifyBus.wait(50);
            }
        }
    }
}
