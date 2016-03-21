package cc.echonet.coolmicapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class StreamStatsService extends IntentService {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    public StreamStatsService() {
        super("StreamStatsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionStatsFetch(Context context, String url) {
        Intent intent = new Intent(context, StreamStatsService.class);
        intent.setAction(Constants.ACTION_STATS_FETCH);
        intent.putExtra(Constants.EXTRA_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_STATS_FETCH.equals(action)) {
                final String url = intent.getStringExtra(Constants.EXTRA_URL);

                StreamStats obj = new StreamStats(-1, -1);

                Intent localIntent = new Intent(Constants.BROADCAST_STREAM_STATS_SERVICE).putExtra(Constants.EXTRA_DATA_STATS_OBJ, obj);
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            }
        }
    }
}
