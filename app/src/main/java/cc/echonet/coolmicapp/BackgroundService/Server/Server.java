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

package cc.echonet.coolmicapp.BackgroundService.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cc.echonet.coolmicapp.BackgroundService.Constants;
import cc.echonet.coolmicapp.BackgroundService.State;
import cc.echonet.coolmicapp.CMTS;
import cc.echonet.coolmicapp.Configuration.Manager;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Volume;
import cc.echonet.coolmicapp.Icecast.Icecast;
import cc.echonet.coolmicapp.Icecast.Request.Stats;
import cc.echonet.coolmicapp.MainActivity;
import cc.echonet.coolmicapp.R;
import cc.echonet.coolmicapp.Utils;
import cc.echonet.coolmicdspjava.CallbackHandler;
import cc.echonet.coolmicdspjava.VUMeterResult;
import cc.echonet.coolmicdspjava.WrapperConstants;

public class Server extends Service implements CallbackHandler {
    private static final String TAG = "BGS/Server";

    private final List<Messenger> clients = new ArrayList<>();
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */

    private final Messenger mMessenger;
    private final IncomingHandler mIncomingHandler;
    private Notification notification = null;
    private Manager manager;
    private Profile profile;

    private final State state;
    private Driver driver;

    private String oldNotificationMessage;
    private String oldNotificationTitle;
    private boolean oldNotificationFlashLed;

    private Icecast icecast;

    public Server() {
        mIncomingHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mIncomingHandler);

        state = new State();
    }

    private Driver getDriver() {
        return driver;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        manager = new Manager(this);
        profile = manager.getCurrentProfile();
        driver = new Driver(this, profile, this);
    }

    private void addClient(Messenger messenger) {
        if (!clients.contains(messenger)) {
            clients.add(messenger);
        }
    }

    synchronized private void updateListeners(int listeners_current, int listeners_peak) {
        state.listeners_current = listeners_current;
        state.listeners_peak = listeners_peak;
    }

    private Message createMessage(int what) {
        return Message.obtain(null, what);
    }

    private Runnable fetchListeners() {
        return () -> {
            try {
                Stats request = icecast.getStats(profile.getServer().getMountpoint());
                cc.echonet.coolmicapp.Icecast.Response.Stats response;

                request.finish();
                response = request.getResponse();

                updateListeners(response.getListenerCurrent(), response.getListenerPeak());

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        private final Server service;

        IncomingHandler(Server service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();

            switch (msg.what) {
                case Constants.C2S_MSG_STATE:
                    if (msg.replyTo != null) {
                        service.addClient(msg.replyTo);
                    }

                    service.checkWrapperState(msg.replyTo);
                    service.sendGain();

                    break;

                case Constants.C2S_MSG_STREAM_ACTION:

                    String profile = data.getString("profile");
                    boolean cmtsTOSAccepted = data.getBoolean("cmtsTOSAccepted", false);

                    service.prepareStream(profile, cmtsTOSAccepted, msg.replyTo);

                    break;

                case Constants.C2S_MSG_STREAM_RELOAD:
                    service.getDriver().reloadParameters();

                    break;

                case Constants.C2S_MSG_STREAM_STOP:
                    service.stopStream(msg.replyTo);

                    break;

                case Constants.C2S_MSG_NEXT_SEGMENT:
                    String path = data.getString("path");

                    Log.d(TAG, "handleMessage: XXX: C2S_MSG_NEXT_SEGMENT: path="+path);

                    try {
                        InputStream inputStream = null;

                        if (path != null) {
                            inputStream = service.getContentResolver().openInputStream(Uri.parse(path));
                        }

                        service.getDriver().nextSegment(inputStream);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;

                case Constants.H2S_MSG_TIMER:
                    if (service.state.uiState == Constants.CONTROL_UI.CONTROL_UI_CONNECTED) {
                        service.state.timerInMS = SystemClock.uptimeMillis() - service.state.startTime;

                        service.postNotification();

                        service.sendStateToAll();

                        if (service.state.lastStateFetch + 15 * 1000 < service.state.timerInMS) {
                            new Thread(service.fetchListeners()).start();

                            service.state.lastStateFetch = service.state.timerInMS;
                        }

                        service.mIncomingHandler.sendEmptyMessageDelayed(Constants.H2S_MSG_TIMER, 500);
                    }

                    break;
                case Constants.C2S_MSG_GAIN:
                    service.setGain(data.getInt("left"), data.getInt("right"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void setGain(int left, int right) {
        if (state.channels != 2) {
            driver.setGain(100, left);
        } else {
            driver.setGain(100, left, right);
        }
    }

    private void sendGain() {
        Message msgReply = createMessage(Constants.C2S_MSG_GAIN);
        Volume volume = profile.getVolume();

        Bundle bundle = msgReply.getData();

        bundle.putInt("left", volume.getLeft());
        bundle.putInt("right", volume.getRight());

        sendMessageToAll(msgReply);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            int what = intent.getExtras().getInt("what");
            Message message = Message.obtain(null, what);

            Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

            Log.d(TAG, "onStartCommand: what:" + what);

            mIncomingHandler.handleMessage(message);
        }

        return ret;
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        state.clientCount++;

        if (state.bindCounts++ == 0) {
            postNotification();
        }

        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        state.clientCount--;
        if (driver.hasCore()) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (nm != null) {
                nm.cancel(Constants.NOTIFICATION_ID_LED);
            }

            stopForeground(true);

            clients.clear();

            stopSelf();
        } else {
            postNotification();

            startForeground(Constants.NOTIFICATION_ID_LED, notification);
        }

        return super.onUnbind(intent);
    }

    private void postNotification() {
        boolean flashLed = (state.uiState == Constants.CONTROL_UI.CONTROL_UI_CONNECTED);
        String title = String.format(Locale.ENGLISH, "State: %s", state.txtState);
        String message = String.format(Locale.ENGLISH, "Listeners: %s", state.getListenersString(getApplicationContext()));

        postNotification(message, title, flashLed);
    }

    private void postNotification(String message, String title, boolean flashLed) {

        if (message.equals(oldNotificationMessage) && title.equals(oldNotificationTitle) && flashLed == oldNotificationFlashLed) {
            return;
        }

        oldNotificationMessage = message;
        oldNotificationTitle = title;
        oldNotificationFlashLed = flashLed;

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_id_channel);
            String description = getString(R.string.notification_id_channel);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);

            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);

            builder = new Notification.Builder(getApplicationContext(), getApplicationContext().getResources().getString(R.string.notification_id_channel));
        }
        else {
            builder = new Notification.Builder(getApplicationContext());
        }

        builder = builder.setOngoing(true).setSmallIcon(R.drawable.icon).setContentIntent(resultPendingIntent).setContentTitle(title).setContentText(message).setOnlyAlertOnce(true);


        if (flashLed) {
            builder.setLights(0xFFff0000, 100, 100);
        }

        notification = builder.build();

        if (nm != null) {
            nm.notify(Constants.NOTIFICATION_ID_LED, notification);
        }
    }

    private void checkWrapperState(Messenger replyTo) {
        Message msgReply = Message.obtain(null, Constants.S2C_MSG_STATE_REPLY, 0, 0);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("state", state);
        bundle.putString("profile", profile.getName());

        try {
            replyTo.send(msgReply);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToAll(Message message) {
        for (Messenger client : clients) {
            try {
                client.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();

                clients.remove(client);
            }
        }

    }

    private void sendStateToAll() {
        for (Messenger client : clients) {
            checkWrapperState(client);
        }
    }

    private void prepareStream(final String profileName, boolean cmtsTOSAccepted, final Messenger replyTo) {
        manager.getGlobalConfiguration().setCurrentProfileName(profileName);
        profile = manager.getCurrentProfile();
        driver.setProfile(profile);

        cc.echonet.coolmicapp.Configuration.Server server = profile.getServer();

        if (icecast != null)
            icecast.close();

        icecast = new Icecast(server.getProtocol(), server.getHostname(), server.getPort());
        icecast.setCredentials(server.getUsername(), server.getPassword());

        if (driver.hasCore()) {
            stopStream(replyTo);
            return;
        }

        if (!Utils.checkRequiredPermissions(getApplicationContext())) {
            Message msgReply = createMessage(Constants.S2C_MSG_PERMISSIONS_MISSING);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }

        if (!driver.isReady()) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_native_components_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Utils.isOnline(this)) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_check_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!profile.getServer().isSet()) {
            Message msgReply = createMessage(Constants.S2C_MSG_CONNECTION_UNSET);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }

        if (CMTS.isCMTSConnection(profile) && !cmtsTOSAccepted) {
            Message msgReply = createMessage(Constants.S2C_MSG_CMTS_TOS);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }

        startStream(replyTo);
    }


    private void startStream(Messenger replyTo) {
        Message msgReply = createMessage(Constants.S2C_MSG_STREAM_START_REPLY);
        Bundle bundle = msgReply.getData();
        boolean success;

        state.timerInMS = 0;
        state.hadError = false;
        state.channels = profile.getAudio().getChannels();

        sendGain();

        try {
            driver.startStream();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();

            Log.e("VS", "Livestream Start: Exception: ", e);

            success = false;

            Toast.makeText(this, R.string.exception_failed_start_general, Toast.LENGTH_SHORT).show();
        }


        bundle.putBoolean("success", success);

        try {
            replyTo.send(msgReply);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopStream(Messenger replyTo) {
        boolean was_running = false;

        Log.d("BS", "Stop Stream");
        was_running = driver.stopStream();
        Log.d("BS", "Past Core Check");

        state.initialConnectPerformed = false;

        if (replyTo != null) {
            Message msgReply = createMessage(Constants.S2C_MSG_STREAM_STOP_REPLY);

            Bundle bundle = msgReply.getData();
            bundle.putBoolean("was_running", was_running);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        sendStateToAll();
    }

    @Override
    @SuppressWarnings("unused")
    public void callbackHandler(WrapperConstants.WrapperCallbackEvents what, int arg0, int arg1) {
        Log.d("CBHandler", String.format("Handler VUMeter: %s Arg0: %d Arg1: %d ", String.valueOf(what), arg0, arg1));

        switch (what) {
            case THREAD_POST_START:
                state.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTING;

                state.txtState = "Connecting";

                break;
            case THREAD_PRE_STOP:
                state.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                state.txtState = "Disconnected";

                if (state.hadError) {
                    driver.stopStream();
                }

                break;
            case THREAD_POST_STOP:

                state.txtState = "Disconnected(post thread stopped)";

                state.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                break;
            case ERROR:
                Message msgReply = createMessage(Constants.S2C_MSG_ERROR);

                Bundle bundle = msgReply.getData();

                bundle.putString("error", getString(R.string.mainactivity_callback_error, Utils.getStringByName(this, "coolmic_error", arg0)));

                sendMessageToAll(msgReply);

                state.hadError = true;

                break;
            case STREAMSTATE:
                String error = "";

                if (arg1 != 0) {
                    error = getString(R.string.txtStateFormatError, arg1);
                }

                /* connected */
                if (arg0 == 2) {
                    state.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTED;
                    state.initialConnectPerformed = true;
                    state.startTime = SystemClock.uptimeMillis();

                    mIncomingHandler.sendEmptyMessageDelayed(Constants.H2S_MSG_TIMER, 500);
                }
                /* disconnected || connectionerror */
                else if (arg0 == 4 || arg0 == 5) {
                    mIncomingHandler.removeMessages(Constants.H2S_MSG_TIMER);

                    if (!state.initialConnectPerformed || !profile.getServer().getReconnect()) {
                        state.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
                    } else {
                        state.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTING;
                    }
                }

                state.txtState = getString(R.string.txtStateFormat, Utils.getStringByName(this, "coolmic_cs", arg0), error);

                break;
            case RECONNECT:
                state.txtState = String.format(getString(R.string.reconnect_in), arg0);

                state.hadError = false;

                break;
            case SEGMENT_CONNECT:
                state.isLive = arg0 != 0;
                break;
            case SEGMENT_DISCONNECT:
                state.isLive = arg0 != 0;
                break;
        }

        postNotification();

        sendStateToAll();
    }

    @Override
    @SuppressWarnings("unused")
    public void callbackVUMeterHandler(VUMeterResult result) {
        Log.d("Handler VUMeter: ", String.valueOf(result.global_power));

        Message msgReply = createMessage(Constants.S2C_MSG_VUMETER);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("vumeterResult", result);

        sendMessageToAll(msgReply);
    }

    @Override
    public void onDestroy() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.v("BG", "Server.onDestroy()");
        stopStream(null);

        nm.cancel(Constants.NOTIFICATION_ID_LED);

        if (icecast != null) {
            icecast.close();
            icecast = null;
        }

        super.onDestroy();
        Log.v("BG", "Server.onDestroy() done");
    }
}
