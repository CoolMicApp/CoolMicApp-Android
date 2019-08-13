package cc.echonet.coolmicapp.BackgroundService.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cc.echonet.coolmicapp.BackgroundService.Constants;
import cc.echonet.coolmicapp.BackgroundService.State;
import cc.echonet.coolmicapp.CMTS;
import cc.echonet.coolmicapp.Configuration.Manager;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Track;
import cc.echonet.coolmicapp.Icecast.Icecast;
import cc.echonet.coolmicapp.Icecast.Request.Stats;
import cc.echonet.coolmicapp.MainActivity;
import cc.echonet.coolmicapp.R;
import cc.echonet.coolmicapp.Utils;
import cc.echonet.coolmicdspjava.VUMeterResult;
import cc.echonet.coolmicdspjava.Wrapper;
import cc.echonet.coolmicdspjava.WrapperConstants;

public class Server extends Service {
    private static final String TAG = "BGS/Server";

    private List<Messenger> clients = new ArrayList<>();
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */

    private final Messenger mMessenger;
    private final IncomingHandler mIncomingHandler;
    private Notification notification = null;
    private Manager manager;
    private Profile profile;

    private State state;

    private String oldNotificationMessage;
    private String oldNotificationTitle;
    private boolean oldNotificationFlashLed;

    private Icecast icecast;

    public Server() {
        mIncomingHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mIncomingHandler);

        state = new State();

        state.wrapperInitializationStatus = Wrapper.getState();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        manager = new Manager(this);
        profile = manager.getCurrentProfile();
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
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Stats request = icecast.getStats(profile.getServer().getMountpoint());
                    cc.echonet.coolmicapp.Icecast.Response.Stats response;

                    request.finish();
                    response = request.getResponse();

                    updateListeners(response.getListenerCurrent(), response.getListenerPeak());

                } catch (Exception e) {
                    e.printStackTrace();
                }
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

                    break;

                case Constants.C2S_MSG_STREAM_ACTION:

                    String profile = data.getString("profile");
                    boolean cmtsTOSAccepted = data.getBoolean("cmtsTOSAccepted", false);

                    service.prepareStream(profile, cmtsTOSAccepted, msg.replyTo);

                    break;

                case Constants.C2S_MSG_STREAM_RELOAD:
                    service.reloadParameters();

                    break;

                case Constants.C2S_MSG_STREAM_STOP:
                    service.stopStream(msg.replyTo);

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
            Wrapper.setMasterGainMono(100, left);
        } else {
            Wrapper.setMasterGainStereo(100, left, right);
        }
    }

    private void sendGain(int left, int right) {
        Message msgReply = createMessage(Constants.C2S_MSG_GAIN);

        Bundle bundle = msgReply.getData();

        bundle.putInt("left", left);
        bundle.putInt("right", right);

        sendMessageToAll(msgReply);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        int what = intent.getExtras().getInt("what");
        Message message = Message.obtain(null, what);

        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

        Log.d(TAG, "onStartCommand: what:" + what);

        mIncomingHandler.handleMessage(message);

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
        if (!hasCore()) {
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
        //String message = String.format(Locale.ENGLISH, "Timer: %s%s Listeners: %s", state.timerString, !flashLed ? "(Stopped)" : "", state.listenersString);
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

    private void checkWrapper() {
        if (state.wrapperInitializationStatus != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            if (state.wrapperInitializationStatus == WrapperConstants.WrapperInitializationStatus.WRAPPER_UNINITIALIZED) {
                if (Wrapper.init() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR) {
                    Log.d("WrapperInit", Wrapper.getInitException().toString());
                    Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_init_error, Toast.LENGTH_SHORT).show();
                }
            } else if (state.wrapperInitializationStatus == WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR) {
                Log.d("WrapperInit", "INIT FAILED");
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_previnit_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_unknown_state, Toast.LENGTH_SHORT).show();
                Log.d("WrapperInit", "INIT STATE UNKNOWN");
            }
        }

        state.wrapperInitializationStatus = Wrapper.getState();
    }

    private void checkWrapperState(Messenger replyTo) {

        checkWrapper();


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

    private boolean hasCore() {
        state.hasCore = Wrapper.getState() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED && Wrapper.hasCore();

        return state.hasCore;
    }

    private void reloadParameters() {
        Track track;

        int ret;

        if (profile == null) {
            return;
        }

        track = profile.getTrack();

        ret = Wrapper.performMetaDataQualityUpdate(track.getTitle(), track.getArtist(), profile.getCodec().getQuality(), 1);

        if (profile.getServer().getReconnect()) {
            ret = Wrapper.setReconnectionProfile("enabled");
        } else {
            ret = Wrapper.setReconnectionProfile("disabled");
        }

    }

    private void prepareStream(final String profileName, boolean cmtsTOSAccepted, final Messenger replyTo) {
        manager.getGlobalConfiguration().setCurrentProfileName(profileName);
        profile = manager.getCurrentProfile();

        cc.echonet.coolmicapp.Configuration.Server server = profile.getServer();

        if (icecast != null)
            icecast.close();

        icecast = new Icecast(server.getProtocol(), server.getHostname(), server.getPort());
        icecast.setCredentials(server.getUsername(), server.getPassword());

        if (hasCore()) {
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

        if (state.wrapperInitializationStatus != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
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

        //setGain(100, 100);
        sendGain(profile.getVolume().getLeft(), profile.getVolume().getRight());

        try {
            String server = profile.getServer().getHostname();
            int port_num = profile.getServer().getPort();

            Log.d("VS", server);
            Log.d("VS", Integer.toString(port_num));

            String username = profile.getServer().getUsername();
            String password = profile.getServer().getPassword();
            String mountpoint = profile.getServer().getMountpoint();
            int sampleRate = profile.getAudio().getSampleRate();
            int channel = profile.getAudio().getChannels();
            double quality = profile.getCodec().getQuality();
            String title = profile.getTrack().getTitle();
            String artist = profile.getTrack().getArtist();
            String codec_string = profile.getCodec().getType();

            int buffersize = AudioRecord.getMinBufferSize(sampleRate, channel == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            Log.d("VS", "Minimum Buffer Size: " + buffersize);
            int status = Wrapper.init(this, server, port_num, username, password, mountpoint, codec_string, sampleRate, channel, buffersize);

            hasCore();

            if (status != 0) {
                throw new Exception("Failed to init Core: " + String.valueOf(status));
            }

            status = Wrapper.performMetaDataQualityUpdate(title, artist, quality, 0);

            if (status != 0) {
                throw new Exception(getString(R.string.exception_failed_metadata_quality, status));
            }

            if (profile.getServer().getReconnect()) {
                status = Wrapper.setReconnectionProfile("enabled");
            } else {
                status = Wrapper.setReconnectionProfile("disabled");
            }

            if (status != 0) {
                throw new Exception(getString(R.string.exception_failed_reconnect, status));
            }

            status = Wrapper.start();

            Log.d("VS", "Status:" + status);

            if (status != 0) {
                throw new Exception(getString(R.string.exception_start_failed, status));
            }

            int interval = profile.getVUMeter().getInterval();

            /* Normalize interval to a sample rate of 48kHz (as per Opus specs). */
            interval = (interval * sampleRate) / 48000;

            Wrapper.setVuMeterInterval(interval);

            success = true;
        } catch (Exception e) {
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
        if (hasCore()) {
            Wrapper.stop();
            Wrapper.unref();
            was_running = true;
        }

        Log.d("BS", "Past Core Check");

        hasCore();

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

    @SuppressWarnings("unused")
    private void callbackHandler(WrapperConstants.WrapperCallbackEvents what, int arg0, int arg1) {
        Log.d("CBHandler", String.format("Handler VUMeter: %s Arg0: %d Arg1: %d ", String.valueOf(what), arg0, arg1));

        state.oldState = state.uiState;

        switch (what) {
            case THREAD_POST_START:
                state.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTING;

                state.txtState = "Connecting";

                break;
            case THREAD_PRE_STOP:
                state.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                state.txtState = "Disconnected";

                if (state.hadError) {
                    Wrapper.stop();
                    Wrapper.unref();
                }

                break;
            case THREAD_POST_STOP:

                state.txtState = "Disconnected(post thread stopped)";

                state.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                break;
            case ERROR:
                 /*
                if(coolmic.getServerReconnect()) {
                    state.uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTING;
                }
                else
                {
                    state.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
                }

                */

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
                    /*if(!state.initialConnectPerformed || !coolmic.getServerReconnect()) {
                     */

                    state.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTED;
                    state.initialConnectPerformed = true;
                    state.startTime = SystemClock.uptimeMillis();

                    mIncomingHandler.sendEmptyMessageDelayed(Constants.H2S_MSG_TIMER, 500);
                        /*
                    }
                    else if(state.initialConnectPerformed && coolmic.getServerReconnect())
                    {
                        state.uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTED;
                    }
                    */
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
                //Toast.makeText(MainActivity.this, getString(R.string.mainactivity_callback_streamstate, arg0_final, arg1_final), Toast.LENGTH_SHORT).show();

                break;
            case RECONNECT:
                state.txtState = String.format(getString(R.string.reconnect_in), arg0);

                state.hadError = false;

                break;
        }

        postNotification();

        sendStateToAll();
    }

    @SuppressWarnings("unused")
    private void callbackVUMeterHandler(VUMeterResult result) {
        Log.d("Handler VUMeter: ", String.valueOf(result.global_power));

        Message msgReply = createMessage(Constants.S2C_MSG_VUMETER);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("vumeterResult", result);

        sendMessageToAll(msgReply);
    }

    @Override
    public void onDestroy() {
        NotificationManager nm;

        Log.v("BG", "Server.onDestroy()");
        stopStream(null);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();

        if (icecast != null) {
            icecast.close();
            icecast = null;
        }

        super.onDestroy();
        Log.v("BG", "Server.onDestroy() done");
    }
}
