package cc.echonet.coolmicapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import cc.echonet.coolmicdspjava.VUMeterResult;
import cc.echonet.coolmicdspjava.Wrapper;
import cc.echonet.coolmicdspjava.WrapperConstants;

public class BackgroundService extends Service {
    private List<Messenger> clients = new ArrayList<>();
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */

    private final Messenger mMessenger;
    private final IncomingHandler mIncomingHandler;
    private Notification notification = null;
    private CoolMic coolmic = null;

    private BackgroundServiceState backgroundServiceState;

    private String oldNotificationMessage;
    private String oldNotificationTitle;
    private boolean oldNotificationFlashLed;

    public BackgroundService() {
        mIncomingHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mIncomingHandler);

        backgroundServiceState = new BackgroundServiceState();

        backgroundServiceState.wrapperInitializationStatus = Wrapper.getState();
    }

    protected void addClient(Messenger messenger) {
        if (!clients.contains(messenger)) {
            clients.add(messenger);
        }
    }

    synchronized private void updateListeners(int listeners_current, int listeners_peak) {
        backgroundServiceState.listenersString = getApplicationContext().getString(R.string.formatListeners, listeners_current, listeners_peak);
    }

    private String exceptionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    private Runnable fetchListeners() {
        final String url = coolmic.getStreamStatsURL();

        return new Runnable() {
            @Override
            public void run() {
                try {
                    Uri u = Uri.parse(url);

                    HttpURLConnection conn = (HttpURLConnection) new URL(u.toString()).openConnection();
                    conn.setUseCaches(false);

                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", "utf-8");
                    conn.setRequestProperty("Accept-Encoding", "text/xml");
                    conn.setRequestProperty("Accept-Language", "en-US");
                    conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(u.getUserInfo().getBytes(), Base64.NO_WRAP));
                    conn.connect();

                    if (conn.getResponseCode() != 200) {
                        Log.e("CM-StreamStatsService", "HTTP error, invalid server status code: " + conn.getResponseMessage());
                    } else {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(conn.getInputStream());

                        Log.d("CM-StreamStatsService", "Parsed Document " + doc.toString());

                        XPathFactory xpathFactory = XPathFactory.newInstance();
                        XPath xpath = xpathFactory.newXPath();

                        Log.d("CM-StreamStatsService", "post xpath");

                        XPathExpression expr_listeners = xpath.compile("/icestats/source/listeners/text()");
                        XPathExpression expr_listeners_peak = xpath.compile("/icestats/source/listener_peak/text()");

                        Log.d("CM-StreamStatsService", "post xpath compile");

                        String listeners = (String) expr_listeners.evaluate(doc, XPathConstants.STRING);
                        String listeners_peak = (String) expr_listeners_peak.evaluate(doc, XPathConstants.STRING);

                        Log.d("CM-StreamStatsService", "post xpath eval " + listeners + " " + listeners_peak);

                        int listenersCurrent = -1;
                        int listenersPeak = -1;

                        if (!listeners.isEmpty()) {
                            listenersCurrent = Integer.valueOf(listeners);
                        } else {
                            Log.d("CM-StreamStatsService", "found no listeners");
                        }

                        if (!listeners_peak.isEmpty()) {
                            listenersPeak = Integer.valueOf(listeners_peak);
                        } else {
                            Log.d("CM-StreamStatsService", "found no listeners peak");
                        }

                        updateListeners(listenersCurrent, listenersPeak);
                    }
                } catch (XPathExpressionException e) {
                    Log.e("CM-StreamStatsService", "XPException while fetching Stats: " + exceptionToString(e));
                } catch (SAXException e) {
                    Log.e("CM-StreamStatsService", "SAXException while fetching Stats: " + exceptionToString(e));
                } catch (ParserConfigurationException e) {
                    Log.e("CM-StreamStatsService", "PCException while fetching Stats: " + exceptionToString(e));
                } catch (IOException e) {
                    Log.e("CM-StreamStatsService", "IOException while fetching Stats: " + exceptionToString(e));
                } catch (Exception e) {
                    Log.e("CM-StreamStatsService", "Exception while fetching Stats: " + exceptionToString(e));
                }
            }
        };
    }

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        private final BackgroundService service;

        IncomingHandler(BackgroundService service) {
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

                    String profile = data.getString("profile", "default");
                    Boolean cmtsTOSAccepted = data.getBoolean("cmtsTOSAccepted", false);

                    service.prepareStream(profile, cmtsTOSAccepted, msg.replyTo);

                    break;

                case Constants.C2S_MSG_STREAM_RELOAD:
                    service.reloadParameters();

                    break;

                case Constants.C2S_MSG_STREAM_STOP:
                    service.stopStream(msg.replyTo);

                    break;

                case Constants.H2S_MSG_TIMER:
                    if (service.backgroundServiceState.uiState == Constants.CONTROL_UI.CONTROL_UI_CONNECTED) {
                        service.backgroundServiceState.timerInMS = SystemClock.uptimeMillis() - service.backgroundServiceState.startTime;

                        int secs = (int) (service.backgroundServiceState.timerInMS / 1000);
                        int mins = secs / 60;
                        int hours = mins / 60;
                        secs = secs % 60;
                        mins = mins % 60;

                        service.backgroundServiceState.timerString = service.getApplicationContext().getString(R.string.timer_format, hours, mins, secs);

                        service.postNotification();

                        service.sendStateToAll();

                        if (service.backgroundServiceState.lastStateFetch + 15 * 1000 < service.backgroundServiceState.timerInMS) {
                            new Thread(service.fetchListeners()).start();

                            service.backgroundServiceState.lastStateFetch = service.backgroundServiceState.timerInMS;
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
        if (backgroundServiceState.channels != 2) {
            Wrapper.setMasterGainMono(100, left);
        } else {
            Wrapper.setMasterGainStereo(100, left, right);
        }
    }

    private void sendGain(int left, int right) {
        Message msgReply = Message.obtain(null, Constants.C2S_MSG_GAIN, 0, 0);

        Bundle bundle = msgReply.getData();

        bundle.putInt("left", left);
        bundle.putInt("right", right);

        sendMessageToAll(msgReply);
    }


    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        backgroundServiceState.clientCount++;

        if (backgroundServiceState.bindCounts++ == 0) {
            postNotification();
        }

        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        backgroundServiceState.clientCount--;
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
        boolean flashLed = (backgroundServiceState.uiState == Constants.CONTROL_UI.CONTROL_UI_CONNECTED);
        String title = String.format(Locale.ENGLISH, "State: %s", backgroundServiceState.txtState);
        //String message = String.format(Locale.ENGLISH, "Timer: %s%s Listeners: %s", backgroundServiceState.timerString, !flashLed ? "(Stopped)" : "", backgroundServiceState.listenersString);
        String message = String.format(Locale.ENGLISH, "Listeners: %s", backgroundServiceState.listenersString);

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
        if (backgroundServiceState.wrapperInitializationStatus != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            if (backgroundServiceState.wrapperInitializationStatus == WrapperConstants.WrapperInitializationStatus.WRAPPER_UNINITIALIZED) {
                if (Wrapper.init() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR) {
                    Log.d("WrapperInit", Wrapper.getInitException().toString());
                    Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_init_error, Toast.LENGTH_SHORT).show();
                }
            } else if (backgroundServiceState.wrapperInitializationStatus == WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR) {
                Log.d("WrapperInit", "INIT FAILED");
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_previnit_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_unknown_state, Toast.LENGTH_SHORT).show();
                Log.d("WrapperInit", "INIT STATE UNKNOWN");
            }
        }

        backgroundServiceState.wrapperInitializationStatus = Wrapper.getState();
    }

    private void checkWrapperState(Messenger replyTo) {

        checkWrapper();


        Message msgReply = Message.obtain(null, Constants.S2C_MSG_STATE_REPLY, 0, 0);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("state", backgroundServiceState);

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
        backgroundServiceState.hasCore = Wrapper.getState() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED && Wrapper.hasCore();

        return backgroundServiceState.hasCore;
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void reloadParameters() {
        int ret;

        if (coolmic == null) {
            return;
        }

        ret = Wrapper.performMetaDataQualityUpdate(coolmic.getTitle(), coolmic.getArtist(), Double.parseDouble(coolmic.getQuality()), 1);

        if (coolmic.getReconnect()) {
            ret = Wrapper.setReconnectionProfile("enabled");
        } else {
            ret = Wrapper.setReconnectionProfile("disabled");
        }

    }

    private void prepareStream(final String profile, boolean cmtsTOSAccepted, final Messenger replyTo) {
        coolmic = new CoolMic(this, profile);

        if (hasCore()) {
            stopStream(replyTo);
            return;
        }

        if (!Utils.checkRequiredPermissions(getApplicationContext())) {
            Message msgReply = Message.obtain(null, Constants.S2C_MSG_PERMISSIONS_MISSING, 0, 0);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }

        if (backgroundServiceState.wrapperInitializationStatus != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_native_components_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_check_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!coolmic.isConnectionSet()) {
            Message msgReply = Message.obtain(null, Constants.S2C_MSG_CONNECTION_UNSET, 0, 0);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }

        if (coolmic.isCMTSConnection() && !cmtsTOSAccepted) {
            Message msgReply = Message.obtain(null, Constants.S2C_MSG_CMTS_TOS, 0, 0);

            try {
                replyTo.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }

        startStream(profile, replyTo);
    }


    private void startStream(String profile, Messenger replyTo) {
        Message msgReply = Message.obtain(null, Constants.S2C_MSG_STREAM_START_REPLY, 0, 0);

        Bundle bundle = msgReply.getData();

        boolean success;

        backgroundServiceState.timerInMS = 0;
        backgroundServiceState.timerString = "00:00:00";
        backgroundServiceState.hadError = false;
        backgroundServiceState.channels = Integer.parseInt(coolmic.getChannels());

        //setGain(100, 100);
        sendGain(coolmic.getVolumeLeft(), coolmic.getVolumeRight());

        try {
            String portnum;
            String server = coolmic.getServerName();
            Integer port_num = 8000;

            if (server.indexOf(":") > 0) {
                String[] split = server.split(":");
                server = split[0];
                portnum = split[1];
                port_num = Integer.parseInt(portnum);
            }

            Log.d("VS", server);
            Log.d("VS", port_num.toString());
            String username = coolmic.getUsername();
            String password = coolmic.getPassword();
            String mountpoint = coolmic.getMountpoint();
            String sampleRate_string = coolmic.getSampleRate();
            String channel_string = coolmic.getChannels();
            String quality_string = coolmic.getQuality();
            String title = coolmic.getTitle();
            String artist = coolmic.getArtist();
            String codec_string = coolmic.getCodec();
            int sampleRate = Integer.parseInt(sampleRate_string);

            Integer buffersize = AudioRecord.getMinBufferSize(Integer.parseInt(sampleRate_string), Integer.parseInt(channel_string) == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            Log.d("VS", "Minimum Buffer Size: " + String.valueOf(buffersize));
            int status = Wrapper.init(this, server, port_num, username, password, mountpoint, codec_string, sampleRate, Integer.parseInt(channel_string), buffersize);

            hasCore();

            if (status != 0) {
                throw new Exception("Failed to init Core: " + String.valueOf(status));
            }

            status = Wrapper.performMetaDataQualityUpdate(title, artist, Double.parseDouble(quality_string), 0);

            if (status != 0) {
                throw new Exception(getString(R.string.exception_failed_metadata_quality, status));
            }

            if (coolmic.getReconnect()) {
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

            int interval = Integer.parseInt(coolmic.getVuMeterInterval());

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

        backgroundServiceState.initialConnectPerformed = false;

        if (replyTo != null) {
            Message msgReply = Message.obtain(null, Constants.S2C_MSG_STREAM_STOP_REPLY, 0, 0);

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

        backgroundServiceState.oldState = backgroundServiceState.uiState;

        switch (what) {
            case THREAD_POST_START:
                backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTING;

                backgroundServiceState.txtState = "Connecting";

                break;
            case THREAD_PRE_STOP:
                backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                backgroundServiceState.txtState = "Disconnected";

                if (backgroundServiceState.hadError) {
                    Wrapper.stop();
                    Wrapper.unref();
                }

                break;
            case THREAD_POST_STOP:

                backgroundServiceState.txtState = "Disconnected(post thread stopped)";

                backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                break;
            case ERROR:
                 /*
                if(coolmic.getReconnect()) {
                    backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTING;
                }
                else
                {
                    backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
                }

                */

                Message msgReply = Message.obtain(null, Constants.S2C_MSG_ERROR, 0, 0);

                Bundle bundle = msgReply.getData();

                bundle.putString("error", getString(R.string.mainactivity_callback_error, Utils.getStringByName(this, "coolmic_error", arg0)));

                sendMessageToAll(msgReply);

                backgroundServiceState.hadError = true;

                break;
            case STREAMSTATE:
                String error = "";

                if (arg1 != 0) {
                    error = getString(R.string.txtStateFormatError, arg1);
                }

                /* connected */
                if (arg0 == 2) {
                    /*if(!backgroundServiceState.initialConnectPerformed || !coolmic.getReconnect()) {
                     */

                    backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTED;
                    backgroundServiceState.initialConnectPerformed = true;
                    backgroundServiceState.startTime = SystemClock.uptimeMillis();

                    mIncomingHandler.sendEmptyMessageDelayed(Constants.H2S_MSG_TIMER, 500);
                        /*
                    }
                    else if(backgroundServiceState.initialConnectPerformed && coolmic.getReconnect())
                    {
                        backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTED;
                    }
                    */
                }
                /* disconnected || connectionerror */
                else if (arg0 == 4 || arg0 == 5) {
                    mIncomingHandler.removeMessages(Constants.H2S_MSG_TIMER);

                    if (!backgroundServiceState.initialConnectPerformed || !coolmic.getReconnect()) {
                        backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
                    } else {
                        backgroundServiceState.uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTING;
                    }
                }

                backgroundServiceState.txtState = getString(R.string.txtStateFormat, Utils.getStringByName(this, "coolmic_cs", arg0), error);
                //Toast.makeText(MainActivity.this, getString(R.string.mainactivity_callback_streamstate, arg0_final, arg1_final), Toast.LENGTH_SHORT).show();

                break;
            case RECONNECT:
                backgroundServiceState.txtState = String.format(getString(R.string.reconnect_in), arg0);

                backgroundServiceState.hadError = false;

                break;
        }

        postNotification();

        sendStateToAll();
    }

    @SuppressWarnings("unused")
    private void callbackVUMeterHandler(VUMeterResult result) {
        Log.d("Handler VUMeter: ", String.valueOf(result.global_power));

        Message msgReply = Message.obtain(null, Constants.S2C_MSG_VUMETER, 0, 0);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("vumeterResult", result);

        sendMessageToAll(msgReply);
    }

    @Override
    public void onDestroy() {
        NotificationManager nm;

        Log.v("BG", "BackgroundService.onDestroy()");
        stopStream(null);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
        super.onDestroy();
        Log.v("BG", "BackgroundService.onDestroy() done");
    }
}
