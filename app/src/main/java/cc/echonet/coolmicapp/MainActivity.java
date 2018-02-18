/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
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
 */
package cc.echonet.coolmicapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import cc.echonet.coolmicdspjava.VUMeterResult;
import cc.echonet.coolmicdspjava.WrapperConstants;

/**
 * This activity demonstrates how to use JNI to encode and decode ogg/vorbis audio
 */
public class MainActivity extends Activity {
    Messenger mBackgroundService = null;
    Messenger mBackgroundServiceClient = null;
    boolean mBackgroundServiceBound = false;

    boolean hasCore = true;
    Constants.CONTROL_UI uiState;
    WrapperConstants.WrapperInitializationStatus wrapperState;


    private ServiceConnection mBackgroundServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mBackgroundService = new Messenger(service);
            mBackgroundServiceBound = true;

            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, Constants.C2S_MSG_STATE, 0, 0);

            msg.replyTo = mBackgroundServiceClient;

            try {
                mBackgroundService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
        private final MainActivity activity;

        IncomingHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.S2C_MSG_STATE_REPLY:
                    Bundle bundle = msg.getData();

                    Constants.CONTROL_UI oldState = activity.uiState;

                    activity.uiState = (Constants.CONTROL_UI) bundle.getSerializable("uiState");
                    activity.wrapperState = (WrapperConstants.WrapperInitializationStatus) bundle.getSerializable("wrapperState");
                    activity.hasCore = bundle.getBoolean("hasCore");

                    if(oldState != activity.uiState) {
                        activity.controlRecordingUI(activity.uiState);
                    }

                    ((TextView) activity.findViewById(R.id.txtState)).setText(bundle.getString("txtState", "unknown"));

                    break;

                case Constants.S2C_MSG_STREAM_START_REPLY:
                    activity.controlVuMeterUI(Integer.parseInt(activity.coolmic.getVuMeterInterval()) != 0);
                    activity.startLock.unlock();

                    activity.controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_CONNECTED);

                    break;

                case Constants.S2C_MSG_STREAM_STOP_REPLY:
                    Toast.makeText(activity, R.string.broadcast_stop_message, Toast.LENGTH_SHORT).show();

                    activity.controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);

                    break;

                case Constants.S2C_MSG_VUMETER:
                    Bundle bundleVUMeter = msg.getData();

                    activity.handleVUMeterResult((VUMeterResult) bundleVUMeter.getSerializable("vumeterResult"));
                default:
                    super.handleMessage(msg);
            }
        }
    }

    MainActivity() {
        this.mBackgroundServiceClient = new Messenger(new IncomingHandler(this));
    }

    final Context context = this;
    CoolMic coolmic = null;
    Button start_button;

    Animation animation = new AlphaAnimation(1, 0);

    ColorDrawable transitionColorGrey = new ColorDrawable(Color.parseColor("#66999999"));
    ColorDrawable transitionColorRed = new ColorDrawable(Color.RED);
    ColorDrawable[] transitionColorDefault = {transitionColorGrey, transitionColorRed};

    TransitionDrawable transitionButton = new TransitionDrawable(transitionColorDefault);

    Drawable buttonColor;
    ImageView imageView1;
    Menu myMenu;
    boolean backyes = false;
    ClipboardManager myClipboard;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    ReentrantLock startLock = new ReentrantLock();

    TextView txtListeners;

    StreamStatsReceiver mStreamStatsReceiver = new StreamStatsReceiver();

    //variable declaration for timer starts here
    private long startTime = 0L;
    private long lastStatsFetch = 0L;
    //code for displaying timer starts here
    Runnable updateTimerThread = new Runnable() {

        @Override
        public void run() {
            runOnUiThread(new Thread(new Runnable() {
                @Override
                public void run() {
                    timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                    updatedTime = timeSwapBuff + timeInMilliseconds;
                    int secs = (int) (updatedTime / 1000);
                    int mins = secs / 60;
                    int hours = mins / 60;
                    secs = secs % 60;
                    mins = mins % 60;

                    timerValue.setText(MainActivity.this.getString(R.string.timer_format, hours, mins, secs));

                    if(lastStatsFetch+15*1000 < timeInMilliseconds) {
                        StreamStatsService.startActionStatsFetch(MainActivity.this, coolmic.getStreamStatsURL());
                        lastStatsFetch = timeInMilliseconds;
                    }
                }
            }));
            customHandler.postDelayed(this, 0);
        }
    };
    private TextView timerValue;
    private Handler customHandler = new Handler();

    //variable declaration for timer ends here
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        /*
        if (hasCore()) {
            menu.findItem(R.id.menu_action_settings).setVisible(false);
            menu.findItem(R.id.menu_action_about).setVisible(false);
        } else {
            menu.findItem(R.id.menu_action_settings).setVisible(true);
            menu.findItem(R.id.menu_action_about).setVisible(true);
        }
        */
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_share:
                performShare();
                return true;
            case R.id.menu_action_settings:
                goSettings();
                return true;
            case R.id.menu_action_about:
                goAbout();
                return true;
            case R.id.menu_action_help:
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.coolmic_help_url)));
                startActivity(helpIntent);
                return true;
            case R.id.menu_action_quit:
                exitApp();
                return true;
            default:
                Toast.makeText(getApplicationContext(), R.string.menu_action_default, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void exitApp() {

        finish();
    }

    private void goSettings() {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    private void goAbout() {
        Intent i = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(i);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageView1.getLayoutParams().height = 180;
        } else {
            imageView1.getLayoutParams().height = 400;
        }
    }

    private boolean checkPermission() {
        return Utils.checkRequiredPermissions(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("$$$$$$", "In Method: onDestroy()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, BackgroundService.class), mBackgroundServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBackgroundServiceBound) {
            unbindService(mBackgroundServiceConnection);
            mBackgroundServiceBound = false;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);
        timerValue = findViewById(R.id.timerValue);
        /*
        BroadcastReceiver mPowerKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String strAction = intent.getAction();
                if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON) || strAction.equals(Intent.ACTION_USER_PRESENT)) {
                    if (hasCore()) {
                        RedFlashLight();
                    }
                }
            }
        };
        final IntentFilter theFilter = new IntentFilter();
        // System Defined Broadcast
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction(Intent.ACTION_USER_PRESENT);

        getApplicationContext().registerReceiver(mPowerKeyReceiver, theFilter);
        */

        imageView1 = findViewById(R.id.imageView1);

        Log.v("onCreate", (imageView1 == null ? "iv null" : "iv ok"));

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        start_button = findViewById(R.id.start_recording_button);
        buttonColor = start_button.getBackground();

        coolmic = new CoolMic(this, "default");

        txtListeners = findViewById(R.id.txtListeners);
        IntentFilter mStatusIntentFilter = new IntentFilter( Constants.BROADCAST_STREAM_STATS_SERVICE );
        LocalBroadcastManager.getInstance(this).registerReceiver(mStreamStatsReceiver, mStatusIntentFilter);


        controlVuMeterUI(Integer.parseInt(coolmic.getVuMeterInterval()) != 0);

        if(mBackgroundServiceBound)
        {
            Message msgReply = Message.obtain(null, Constants.C2S_MSG_STATE, 0, 0);
            msgReply.replyTo = mBackgroundServiceClient;
            try {
                mBackgroundService.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /*
        if(hasCore())
        {
            controlRecordingUI(CONTROL_UI.CONTROL_UI_CONNECTED);
        }
        */
    }

    public void onImageClick(View view) {
        if (coolmic.isConnectionSet()) {
            ClipData myClip = ClipData.newPlainText("text", coolmic.getStreamURL());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(getApplicationContext(), R.string.mainactivity_broadcast_url_copied, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_connectiondetails_unset, Toast.LENGTH_SHORT).show();
        }
    }

    public void performShare() {
        if (coolmic.isConnectionSet()) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, coolmic.getStreamURL());
            shareIntent.setType("text/plain");

            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.menu_action_share_title)));

        } else {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_connectiondetails_unset, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Write your code here
        /*
        if (hasCore()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle(R.string.question_stop_broadcasting);
            alertDialog.setMessage(R.string.coolmic_back_message);
            alertDialog.setNegativeButton(R.string.mainactivity_quit_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    backyes = false;
                    dialog.cancel();
                }
            });
            alertDialog.setPositiveButton(R.string.mainactivity_quit_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    backyes = true;
                    dialog.cancel();

                    controlRecordingUI(CONTROL_UI.CONTROL_UI_DISCONNECTED);

                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alertDialog.show();
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        */
    }

    public void controlRecordingUI(Constants.CONTROL_UI state) {
        if(state == Constants.CONTROL_UI.CONTROL_UI_CONNECTING)
        {
            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);
            transitionButton.startTransition(5000);

            start_button.setText(R.string.cmdStartInitializing);
            start_button.setEnabled(false);

            controlTimerThread(true, 0);
        }
        else if(state == Constants.CONTROL_UI.CONTROL_UI_CONNECTED)
        {
            startService(new Intent(getBaseContext(), MyService.class));

            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);

            start_button.setText(R.string.broadcasting);
            start_button.setEnabled(true);
        }
        else if(state == Constants.CONTROL_UI.CONTROL_UI_RECONNECTING)
        {
            controlTimerThread(false);

            start_button.setText(R.string.reconnecting);
            start_button.setEnabled(true);
        }
        else if(state == Constants.CONTROL_UI.CONTROL_UI_RECONNECTED)
        {
            controlTimerThread(true, timeSwapBuff);

            start_button.clearAnimation();
            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);

            start_button.setText(R.string.broadcasting);
            start_button.setEnabled(true);
        }
        else
        {
            start_button.clearAnimation();
            start_button.setBackground(buttonColor);
            start_button.setText(R.string.start_broadcast);
            start_button.setEnabled(true);

            timeSwapBuff += timeInMilliseconds;
            customHandler.removeCallbacks(updateTimerThread);

            ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setProgress(0);
            ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setProgress(0);
            ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setText("");
            ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setText("");
            ((TextView) MainActivity.this.findViewById(R.id.rbPeakLeft)).setText("");
            ((TextView) MainActivity.this.findViewById(R.id.rbPeakRight)).setText("");

            controlTimerThread(false);
        }
    }

    public void controlTimerThread(boolean running)
    {
        controlTimerThread(running, -1);
    }

    public void controlTimerThread(boolean running, long timerStartTime)
    {
        if(running) {
            timeInMilliseconds = timerStartTime;
            timeSwapBuff = timerStartTime;
            updatedTime = timerStartTime;
            timeSwapBuff += timeInMilliseconds;
            customHandler.removeCallbacks(updateTimerThread);
            startTime = SystemClock.uptimeMillis();
            customHandler.postDelayed(updateTimerThread, 0);
        }
        else {
            timeSwapBuff += timeInMilliseconds;
            customHandler.removeCallbacks(updateTimerThread);
        }
    }

    public void controlVuMeterUI(boolean visible)
    {
        if(findViewById(R.id.llVuMeterLeft) != null)
        {
            findViewById(R.id.llVuMeterLeft).setVisibility((visible ? View.VISIBLE : View.GONE));
        }

        if(findViewById(R.id.llVuMeterRight) != null)
        {
            findViewById(R.id.llVuMeterRight).setVisibility((visible ? View.VISIBLE : View.GONE));
        }

        findViewById(R.id.pbVuMeterLeft).setVisibility((visible ? View.VISIBLE : View.GONE));
        findViewById(R.id.pbVuMeterRight).setVisibility((visible ? View.VISIBLE : View.GONE));
        findViewById(R.id.rbPeakLeft).setVisibility((visible ? View.VISIBLE : View.GONE));
        findViewById(R.id.rbPeakRight).setVisibility((visible ? View.VISIBLE : View.GONE));
    }

    public void startRecording(View view) {
        if (!startLock.tryLock()) {
            return;
        }

        controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_CONNECTING);

        if (hasCore) {
            stopRecording(view);
            return;
        }

        if (!checkPermission()) {
            startLock.unlock();
            controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);

            Utils.requestPermissions(this);

            return;
        }

        if (wrapperState != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_native_components_not_ready, Toast.LENGTH_SHORT).show();
            startLock.unlock();
            controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);
            return;
        }

        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_check_connection, Toast.LENGTH_SHORT).show();
            startLock.unlock();
            controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);
            return;
        }

        if (!coolmic.isConnectionSet()) {
            startLock.unlock();
            controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.mainactivity_missing_connection_details_title);
            alertDialog.setMessage(R.string.mainactivity_missing_connection_details_body);
            alertDialog.setNegativeButton(R.string.mainactivity_missing_connection_details_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.setPositiveButton(R.string.mainactivity_missing_connection_details_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Utils.loadCMTSData(MainActivity.this, "default");
                    startRecording(null);
                }
            });

            alertDialog.show();

            return;
        }

        if(coolmic.isCMTSConnection()) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.coolmic_tos_title);
            alertDialog.setMessage(R.string.coolmic_tos);
            alertDialog.setNegativeButton(R.string.coolmic_tos_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startLock.unlock();
                    controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);
                    dialog.cancel();
                }
            });
            alertDialog.setPositiveButton(R.string.coolmic_tos_accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startRecordingStep2();
                }
            });

            alertDialog.show();
        }
        else
        {
            startRecordingStep2();
        }
    }

    void startRecordingStep2()
    {
        invalidateOptionsMenu();

        if(mBackgroundServiceBound)
        {
            Message msgReply = Message.obtain(null, Constants.C2S_MSG_STREAM_START, 0, 0);

            msgReply.replyTo = mBackgroundServiceClient;

            Bundle bundle = msgReply.getData();

            bundle.putString("profile", "default");

            try {
                mBackgroundService.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecording(@SuppressWarnings("unused") View view) {
        if(wrapperState != WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_native_components_not_ready, Toast.LENGTH_SHORT).show();
        }

        if(!hasCore)
        {
            return;
        }

        controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);

        if(startLock.isHeldByCurrentThread()) {
            startLock.unlock();
        }

        if(mBackgroundServiceBound)
        {
            Message msgReply = Message.obtain(null, Constants.C2S_MSG_STREAM_STOP, 0, 0);
            msgReply.replyTo = mBackgroundServiceClient;
            try {
                mBackgroundService.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(!Utils.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else
        {
            if(checkPermission())
            {
                startRecording(null);
            }
        }


    }


    static int normalizeVUMeterPower(double power)
    {
        int g_p = (int)((60.+power) * (100. / 60.));

        if(g_p > 100)
        {
            g_p = 100;
        }

        if(g_p < 0)
        {
            g_p = 0;
        }

        return g_p;
    }

    static String normalizeVUMeterPeak(int peak)
    {
        if(peak == -32768 || peak == 32767)
        {
            return "P";
        }
        else if(peak < -30000 || peak > 30000)
        {
            return "p";
        }
        else if(peak < -8000 || peak > 8000)
        {
            return "g";
        }
        else
        {
            return "";
        }
    }

    static String normalizeVUMeterPowerString(double power)
    {
        if(power < -100)
        {
            return "-100";
        }
        else if(power > 0)
        {
            return "0";
        }
        else
        {
            return String.format(Locale.ENGLISH, "%.2f", power);
        }
    }

    public void handleVUMeterResult(VUMeterResult vuMeterResult) {
        TextProgressBar pbVuMeterLeft = this.findViewById(R.id.pbVuMeterLeft);
        TextProgressBar pbVuMeterRight = this.findViewById(R.id.pbVuMeterRight);

        TextView rbPeakLeft = this.findViewById(R.id.rbPeakLeft);
        TextView rbPeakRight = this.findViewById(R.id.rbPeakRight);

        if(vuMeterResult.channels < 2) {
            pbVuMeterLeft.setProgress(normalizeVUMeterPower(vuMeterResult.global_power));
            pbVuMeterLeft.setTextColor(vuMeterResult.global_power_color);
            pbVuMeterLeft.setText(normalizeVUMeterPowerString(vuMeterResult.global_power));
            pbVuMeterRight.setProgress(normalizeVUMeterPower(vuMeterResult.global_power));
            pbVuMeterRight.setTextColor(vuMeterResult.global_power_color);
            pbVuMeterRight.setText(normalizeVUMeterPowerString(vuMeterResult.global_power));
            rbPeakLeft.setText(normalizeVUMeterPeak(vuMeterResult.global_peak));
            rbPeakLeft.setTextColor(vuMeterResult.global_peak_color);
            rbPeakRight.setText(normalizeVUMeterPeak(vuMeterResult.global_peak));
            rbPeakRight.setTextColor(vuMeterResult.global_peak_color);
        }
        else
        {
            pbVuMeterLeft.setProgress(normalizeVUMeterPower(vuMeterResult.channels_power[0]));
            pbVuMeterLeft.setTextColor(vuMeterResult.channels_power_color[0]);
            pbVuMeterLeft.setText(normalizeVUMeterPowerString(vuMeterResult.channels_power[0]));
            pbVuMeterRight.setProgress(normalizeVUMeterPower(vuMeterResult.channels_power[1]));
            pbVuMeterRight.setTextColor(vuMeterResult.channels_power_color[1]);
            pbVuMeterRight.setText(normalizeVUMeterPowerString(vuMeterResult.channels_power[1]));
            rbPeakLeft.setText(normalizeVUMeterPeak(vuMeterResult.channels_peak[0]));
            rbPeakLeft.setTextColor(vuMeterResult.channels_peak_color[0]);
            rbPeakRight.setText(normalizeVUMeterPeak(vuMeterResult.channels_peak[1]));
            rbPeakRight.setTextColor(vuMeterResult.channels_peak_color[1]);
        }
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class StreamStatsReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private StreamStatsReceiver() {
        }
        // Called when the BroadcastReceiver gets an Intent it's registered to receive

        public void onReceive(Context context, Intent intent) {
            StreamStats obj = intent.getParcelableExtra(Constants.EXTRA_DATA_STATS_OBJ);

            txtListeners.setText(context.getString(R.string.formatListeners, obj.getListenersCurrent(), obj.getListenersPeak()));
        }
    }
}
