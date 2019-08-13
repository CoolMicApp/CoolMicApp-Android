package cc.echonet.coolmicapp.BackgroundService.Client;

import android.util.Log;

public class SyncOnce {
    private static final String TAG = "BGS/Client/SyncOnce";

    private final Object notifyBus = new Object();
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
