package cc.echonet.coolmicapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
    private Constants.CONTROL_UI uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
    private String txtState = "N/A";
    private CoolMic coolmic = null;
    private static int serviceRestarts = -1;

    public BackgroundService() {
        mIncomingHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mIncomingHandler);

    }

    protected void addClient(Messenger messenger) {
        if(!clients.contains(messenger)) {
            clients.add(messenger);
        }
    }

    protected void removeClient(Messenger messenger) {
        clients.remove(messenger);
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
            switch (msg.what) {
                case Constants.C2S_MSG_STATE:
                    if(msg.replyTo != null) {
                        service.addClient(msg.replyTo);
                    }

                    service.checkWrapperState(msg.replyTo);

                    break;

                case Constants.C2S_MSG_STREAM_START:

                    Bundle data = msg.getData();

                    String profile = data.getString("profile", "default");

                    service.startStream(profile, msg.replyTo);

                    break;
                case Constants.C2S_MSG_STREAM_STOP:

                    service.stopStream(msg.replyTo);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();

        postNotification("Background Service Bound", "Background Service Bound", false);

        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(), "unbind", Toast.LENGTH_SHORT).show();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(nm != null) {
            nm.cancel(Constants.NOTIFICATION_ID_LED);
        }

        clients.clear();

        /*
        if(hasCore())
        {
            Wrapper.stop();
            Wrapper.unref();
        }



        */

        if(!hasCore())
        {
            stopSelf();
        }




        return super.onUnbind(intent);
    }

    private void postNotification(String message, String title, boolean flashLed) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification.Builder builder = new Notification.Builder(getApplicationContext()).setOngoing(true).setSmallIcon(R.drawable.icon).setContentIntent(resultPendingIntent).setContentTitle(title).setContentText(message);

        if(flashLed)
        {
            builder.setLights(0xFFff0000, 100, 100);
        }

        Notification notif = builder.build();

        if(nm != null) {
            nm.notify(Constants.NOTIFICATION_ID_LED, notif);
        }

        startForeground(Constants.NOTIFICATION_ID_LED, notif);
    }

    private void checkWrapperState(Messenger replyTo) {

        if(Wrapper.getState() != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            if (Wrapper.getState() == WrapperConstants.WrapperInitializationStatus.WRAPPER_UNINITIALIZED) {
                if (Wrapper.init() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR) {
                    Log.d("WrapperInit", Wrapper.getInitException().toString());
                    Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_init_error, Toast.LENGTH_SHORT).show();
                }
            } else if (Wrapper.getState() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR) {
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_previnit_error, Toast.LENGTH_SHORT).show();
            } else if (Wrapper.getState() != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_unknown_state, Toast.LENGTH_SHORT).show();
            }
        }

        Message msgReply = Message.obtain(null, Constants.S2C_MSG_STATE_REPLY, 0, 0);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("wrapperState", Wrapper.getState());
        bundle.putBoolean("hasCore", hasCore());
        bundle.putSerializable("uiState", uiState);
        bundle.putString("txtState", txtState);

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
        return Wrapper.getState() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED && Wrapper.hasCore();
    }

    private void startStream(String profile, Messenger replyTo) {
        coolmic = new CoolMic(this, profile);

        Message msgReply = Message.obtain(null, Constants.S2C_MSG_STREAM_START_REPLY, 0, 0);

        Bundle bundle = msgReply.getData();

        boolean success;

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

            Integer buffersize = AudioRecord.getMinBufferSize(Integer.parseInt(sampleRate_string), Integer.parseInt(channel_string) == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            Log.d("VS", "Minimum Buffer Size: " + String.valueOf(buffersize));
            int status = Wrapper.init(this, server, port_num, username, password, mountpoint, codec_string, Integer.parseInt(sampleRate_string), Integer.parseInt(channel_string), buffersize);

            if(status != 0)
            {
                throw new Exception("Failed to init Core: "+String.valueOf(status));
            }

            status = Wrapper.performMetaDataQualityUpdate(title, artist, Double.parseDouble(quality_string), 0);

            if(status != 0)
            {
                throw new Exception(getString(R.string.exception_failed_metadata_quality, status));
            }

            if(coolmic.getReconnect()) {
                status = Wrapper.setReconnectionProfile("enabled");
            }
            else
            {
                status = Wrapper.setReconnectionProfile("disabled");
            }

            if(status != 0)
            {
                throw new Exception(getString(R.string.exception_failed_reconnect, status));
            }

            status = Wrapper.start();

            Log.d("VS", "Status:" + status);

            if(status != 0)
            {
                throw new Exception(getString(R.string.exception_start_failed, status));
            }

            int interval = Integer.parseInt(coolmic.getVuMeterInterval());

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
        if(hasCore())
        {
            Wrapper.stop();
            Wrapper.unref();
        }

        Message msgReply = Message.obtain(null, Constants.S2C_MSG_STREAM_STOP_REPLY, 0, 0);

        try {
            replyTo.send(msgReply);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        sendStateToAll();
    }

    @SuppressWarnings("unused")
    private void callbackHandler(int what, int arg0, int arg1)
    {
        Log.d("CBHandler", String.format("Handler VUMeter: %s Arg0: %d Arg1: %d ", String.valueOf(what), arg0, arg1));

        Constants.CONTROL_UI oldState = uiState;

         switch(what) {
            case 1:
                uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTING;

                txtState = "connecting";

                break;
            case 2:
                uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

                txtState = "disconnected";

                break;
            case 3:
                if(coolmic.getReconnect()) {
                    uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTING;
                }
                else
                {
                    uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
                }

                txtState = getString(R.string.mainactivity_callback_error, arg0);

                break;
            case 4:
                String error = "";

                if(arg1 != 0)
                {
                    error = getString(R.string.txtStateFormatError, arg1);
                }

                if(arg0 == 2)
                {
                    if(!coolmic.getReconnect()) {
                        uiState = Constants.CONTROL_UI.CONTROL_UI_CONNECTED;
                    }
                    else
                    {
                        uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTED;
                    }
                }
                else if(arg0 == 4 || arg0 == 5)
                {
                    if(!coolmic.getReconnect()) {
                        uiState = Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;
                    }
                    else
                    {
                        uiState = Constants.CONTROL_UI.CONTROL_UI_RECONNECTING;
                    }
                }

               txtState = getString(R.string.txtStateFormat, Utils.getStringByName(this, "coolmic_cs", arg0), error);
                //Toast.makeText(MainActivity.this, getString(R.string.mainactivity_callback_streamstate, arg0_final, arg1_final), Toast.LENGTH_SHORT).show();

                break;
            case 5:
                txtState = String.format("reconnect in %d sec", arg0);

                break;
        }

        if(oldState != uiState)
        {
            switch(uiState) {
                case CONTROL_UI_CONNECTING:
                    postNotification("connecting", "connecting", false);
                    break;

                case CONTROL_UI_CONNECTED:
                    postNotification("connected", "connected", true);
                    break;

                case CONTROL_UI_RECONNECTING:
                    postNotification("reconnecting", "reconnecting", false);
                    break;

                case CONTROL_UI_RECONNECTED:
                    postNotification("reconnected", "reconnected", false);
                    break;

                default:
                case CONTROL_UI_DISCONNECTED:
                    postNotification("disconnected", "disconnected", false);
                    break;
            }
        }

        sendStateToAll();
    }

    @SuppressWarnings("unused")
    private void callbackVUMeterHandler(VUMeterResult result)
    {
        Log.d("Handler VUMeter: ", String.valueOf(result.global_power));

        Message msgReply = Message.obtain(null, Constants.S2C_MSG_VUMETER, 0, 0);

        Bundle bundle = msgReply.getData();

        bundle.putSerializable("vumeterResult", result);

        sendMessageToAll(msgReply);
    }
}
