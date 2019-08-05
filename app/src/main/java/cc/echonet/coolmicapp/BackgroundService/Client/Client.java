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
    private Context context;
    private EventListener eventListener;
    private Messenger mBackgroundService = null;
    private Messenger mBackgroundServiceClient = new Messenger(new IncomingHandler(this));
    private boolean mBackgroundServiceBound = false;
    private Profile profile;

    private ServiceConnection mBackgroundServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mBackgroundService = new Messenger(service);
            mBackgroundServiceBound = true;

            sendMessage(Constants.C2S_MSG_STATE);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mBackgroundService = null;
            mBackgroundServiceBound = false;
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
            Bundle bundle = msg.getData();

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

    private void sendMessage(Message message) {
        try {
            mBackgroundService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {
        sendMessage(createMessage(what));
    }

    public Client(Context context, EventListener eventListener) {
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

    public void connect() {
        if (mBackgroundServiceBound)
            return;

        Intent intent = new Intent(context, Server.class);
        context.startService(intent);
        context.bindService(intent, mBackgroundServiceConnection, Context.BIND_AUTO_CREATE);
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
