package cc.echonet.coolmicapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service {
    private static final int LED_NOTIFICATION_ID = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    public void onTaskRemoved(Intent rootIntent) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(LED_NOTIFICATION_ID);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}


