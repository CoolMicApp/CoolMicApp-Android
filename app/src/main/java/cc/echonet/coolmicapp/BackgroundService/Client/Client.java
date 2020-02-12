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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.Closeable;

import cc.echonet.coolmicapp.BackgroundService.Constants;
import cc.echonet.coolmicapp.BackgroundService.Server.Server;
import cc.echonet.coolmicapp.BackgroundService.State;
import cc.echonet.coolmicapp.Configuration.Manager;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.R;
import cc.echonet.coolmicdspjava.VUMeterResult;

public class Client implements Closeable {
    private static final String TAG = "BGS/Client";

    private final Context context;
    private EventListener eventListener;
    private Messenger mBackgroundService = null;
    private final Messenger mBackgroundServiceClient = new Messenger(new IncomingHandler(this));
    private boolean mBackgroundServiceBound = false;
    private Profile profile;
    private SyncOnce ready = new SyncOnce();
    private boolean connectRequested = false;

    private final ServiceConnection mBackgroundServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.

            Log.d(TAG, "onServiceConnected(): Client.this = " + Client.this);
            synchronized (Client.this) {
                Log.d(TAG, "onServiceConnected(): sync outer in");

                synchronized (Client.this) {
                    mBackgroundService = new Messenger(service);
                    mBackgroundServiceBound = true;

                    Log.d(TAG, "onServiceConnected: Client.context = " + Client.this.context + ", Client.this = " + Client.this + ", ready=" + ready);

                    synchronized (Client.this) {
                        ready.ready();
                    }

                    Log.d(TAG, "onServiceConnected(): sync outer out");
                }
            }

            sendMessage(Constants.C2S_MSG_STATE);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.

            Log.d(TAG, "onServiceDisconnected: className=" + className + ", Client.this=" + Client.this);

            synchronized (Client.this) {
                ready = new SyncOnce();
            }

            mBackgroundService = null;
            mBackgroundServiceBound = false;
        }

        @Override
        public void onBindingDied(ComponentName className) {
            Log.d(TAG, "onBindingDied: className=" + className + ", Client.this=" + Client.this);
        }
    };

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        private final Client client;
        private boolean isConnected = false;

        IncomingHandler(Client client) {
            this.client = client;
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;

            if (client.eventListener == null) {
                super.handleMessage(msg);
                return;
            }

            bundle = msg.getData();

            switch (msg.what) {
                case Constants.S2C_MSG_STATE_REPLY:
                    State state = (State) bundle.getSerializable("state");
                    String profileName = bundle.getString("profile");

                    if (state == null || profileName == null)
                        return;

                    Log.v("IH", "In Handler: S2C_MSG_STATE_REPLY: State=" + state.uiState + ", profileName=" + profileName);

                    if (client.profile == null || !client.profile.getName().equals(profileName)) {
                        /* We have a new profile */

                        Manager manager = new Manager(client.context);

                        client.profile = manager.getProfile(profileName);
                    }

                    if (!isConnected) {
                        client.eventListener.onBackgroundServiceConnected();
                        isConnected = true;
                    }

                    client.eventListener.onBackgroundServiceState(state);
                    break;

                case Constants.S2C_MSG_ERROR:
                    String error = bundle.getString("error");

                    Log.v("IH", "In Handler: S2C_MSG_ERROR: error=" + error);

                    Toast.makeText(client.context, error, Toast.LENGTH_LONG).show();

                    client.eventListener.onBackgroundServiceError();

                    break;
                case Constants.S2C_MSG_STREAM_START_REPLY:
                    client.eventListener.onBackgroundServiceStartRecording();

                    Log.v("IH", "In Handler: S2C_MSG_STREAM_START_REPLY: X!");
                    break;

                case Constants.S2C_MSG_STREAM_STOP_REPLY:
                    boolean was_running = bundle.getBoolean("was_running");

                    Log.v("IH", "In Handler: S2C_MSG_STREAM_STOP_REPLY: X!");

                    if (was_running) {
                        Toast.makeText(client.context, R.string.broadcast_stop_message, Toast.LENGTH_SHORT).show();
                    }

                    client.eventListener.onBackgroundServiceStopRecording();
                    break;

                case Constants.S2C_MSG_PERMISSIONS_MISSING:
                    client.eventListener.onBackgroundServicePermissionsMissing();
                    break;

                case Constants.S2C_MSG_CONNECTION_UNSET:
                    client.eventListener.onBackgroundServiceConnectionUnset();
                    break;

                case Constants.S2C_MSG_CMTS_TOS:
                    client.eventListener.onBackgroundServiceCMTSTOSAcceptMissing();
                    break;

                case Constants.S2C_MSG_VUMETER:
                    Bundle bundleVUMeter = msg.getData();
                    client.eventListener.onBackgroundServiceVUMeterUpdate((VUMeterResult) bundleVUMeter.getSerializable("vumeterResult"));
                    break;

                case Constants.C2S_MSG_GAIN:
                    Bundle bundleGain = msg.getData();
                    client.eventListener.onBackgroundServiceGainUpdate(bundleGain.getInt("left"), bundleGain.getInt("right"));
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Message createMessage(int what) {
        Message message = Message.obtain(null, what);

        message.replyTo = mBackgroundServiceClient;

        return message;
    }

    private synchronized void sync() throws InterruptedException {
        if (mBackgroundServiceBound)
            return;

        Log.d(TAG, "sync(): sync in, this=" + this + ", context=" + context + ", ready=" + ready);
        ready.sync();
        Log.d(TAG, "sync(): sync out, this=" + this + ", context=" + context + ", ready=" + ready);
    }

    private synchronized void sendMessage(Message message) {
        Log.d(TAG, "sendMessage(): this = " + this + ", mBackgroundServiceBound = " + mBackgroundServiceBound);

        try {
            sync();
        } catch (InterruptedException ignored) {
        }

        try {
            mBackgroundService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {
        if (connectRequested) {
            sendMessage(createMessage(what));
        } else {
            doStartService(what);
        }
    }

    public Client(Context context, EventListener eventListener) {
        Log.d(TAG, "Client: this=" + this + ", context=" + context + ", eventListener=" + eventListener);
        this.context = context;
        this.eventListener = eventListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public Profile getProfile() {
        return profile;
    }

    public void startRecording(boolean cmtsTOSAccepted, String profileName) {
        Message message = createMessage(Constants.C2S_MSG_STREAM_ACTION);
        Bundle bundle = message.getData();

        bundle.putString("profile", profileName);
        bundle.putBoolean("cmtsTOSAccepted", cmtsTOSAccepted);

        sendMessage(message);
    }

    public void stopRecording() {
        sendMessage(Constants.C2S_MSG_STREAM_STOP);
    }

    public void setGain(int left, int right) {
        Message message = createMessage(Constants.C2S_MSG_GAIN);
        Bundle bundle = message.getData();

        bundle.putInt("left", left);
        bundle.putInt("right", right);

        sendMessage(message);
    }

    public void reloadParameters() {
        sendMessage(Constants.C2S_MSG_STREAM_RELOAD);
    }

    public void nextSegment(String path) {
        Message message = createMessage(Constants.C2S_MSG_NEXT_SEGMENT);
        Bundle bundle = message.getData();

        bundle.putString("path", path);

        sendMessage(message);
    }

    private Intent doStartService(int what) {
        Intent intent = new Intent(context, Server.class);
        intent.putExtra("what", what);
        context.startService(intent);
        return intent;
    }

    public void connect() {
        Intent intent;

        Log.d(TAG, "connect() called");

        if (mBackgroundServiceBound) {
            Log.d(TAG, "connect() = (void)");
            return;
        }

        connectRequested = true;

        Log.d(TAG, "connect() tries to bind.");
        intent = doStartService(Constants.A2A_MSG_NONE);
        context.bindService(intent, mBackgroundServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "connect() = (void)");
    }

    public void disconnect() {
        if (!mBackgroundServiceBound)
            return;

        context.unbindService(mBackgroundServiceConnection);
        mBackgroundServiceBound = false;
        eventListener.onBackgroundServiceDisconnected();
    }

    @Override
    public void close() {
        disconnect();
    }
}
