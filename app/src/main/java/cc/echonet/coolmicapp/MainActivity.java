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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
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
import cc.echonet.coolmicdspjava.Wrapper;

/**
 * This activity demonstrates how to use JNI to encode and decode ogg/vorbis audio
 */
public class MainActivity extends Activity {


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
        if (hasCore()) {
            menu.findItem(R.id.menu_action_settings).setVisible(false);
            menu.findItem(R.id.menu_action_about).setVisible(false);
        } else {
            menu.findItem(R.id.menu_action_settings).setVisible(true);
            menu.findItem(R.id.menu_action_about).setVisible(true);
        }
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
        ClearLED();

        if(hasCore()) {
            Wrapper.stop();
            Wrapper.unref();
        }

        finish();
        System.exit(0);
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
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
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

    private void RedFlashLight() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notif = new Notification.Builder(context).setLights(0xFFff0000, 100, 100).setOngoing(true).setSmallIcon(R.drawable.icon).setContentIntent(resultPendingIntent).setContentTitle("Streaming").setContentText("Streaming...").build();

        nm.notify(Constants.NOTIFICATION_ID_LED, notif);
    }

    private void ClearLED() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(Constants.NOTIFICATION_ID_LED);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasCore()) {
            RedFlashLight();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("$$$$$$", "In Method: onDestroy()");

        if(hasCore())
        {
            ClearLED();
            stopRecording(null);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);
        timerValue = (TextView) findViewById(R.id.timerValue);
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
        /** System Defined Broadcast */
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction(Intent.ACTION_USER_PRESENT);

        getApplicationContext().registerReceiver(mPowerKeyReceiver, theFilter);

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        Log.v("onCreate", (imageView1 == null ? "iv null" : "iv ok"));

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        start_button = (Button) findViewById(R.id.start_recording_button);
        buttonColor = start_button.getBackground();

        coolmic = new CoolMic(this, "default");

        if(Wrapper.getState() == Wrapper.WrapperInitializationStatus.WRAPPER_UNINITIALIZED)
        {
            if(Wrapper.init() == Wrapper.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR)
            {
                Log.d("WrapperInit", Wrapper.getInitException().toString());
                Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_init_error, Toast.LENGTH_SHORT).show();
            }
        }
        else if(Wrapper.getState() == Wrapper.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR)
        {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_previnit_error, Toast.LENGTH_SHORT).show();
        }
        else if(Wrapper.getState() != Wrapper.WrapperInitializationStatus.WRAPPER_INTITIALIZED)
        {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_native_components_unknown_state, Toast.LENGTH_SHORT).show();
        }

        txtListeners = (TextView) findViewById(R.id.txtListeners);
        IntentFilter mStatusIntentFilter = new IntentFilter( Constants.BROADCAST_STREAM_STATS_SERVICE );
        LocalBroadcastManager.getInstance(this).registerReceiver(mStreamStatsReceiver, mStatusIntentFilter);


        controlVuMeterUI(Integer.parseInt(coolmic.getVuMeterInterval()) != 0);

        if(hasCore())
        {
            controlRecordingUI(true);
        }
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

                    controlRecordingUI(false);

                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alertDialog.show();
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public boolean hasCore() {
        return Wrapper.getState() == Wrapper.WrapperInitializationStatus.WRAPPER_INTITIALIZED && Wrapper.hasCore();
    }

    public void controlButtonState(int state)
    {
        switch(state)
        {
            case 0:
                start_button.clearAnimation();
                start_button.setBackground(buttonColor);
                start_button.setText(R.string.start_broadcast);
                start_button.setEnabled(true);
                break;

            case 1:
                start_button.startAnimation(animation);
                start_button.setBackground(transitionButton);
                transitionButton.startTransition(5000);

                start_button.setText(R.string.cmdStartInitializing);
                start_button.setEnabled(false);
                break;

            case 2:
                start_button.setText(R.string.broadcasting);
                start_button.setEnabled(true);
                break;
        }
    }

    public void controlRecordingUI(boolean running) {

        if(running)
        {
            startService(new Intent(getBaseContext(), MyService.class));
            RedFlashLight();

            controlButtonState(2);
        }
        else
        {
            invalidateOptionsMenu();

            controlButtonState(0);

            ClearLED();

            ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setProgress(0);
            ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setProgress(0);
            ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setText("");
            ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setText("");
            ((TextView) MainActivity.this.findViewById(R.id.rbPeakLeft)).setText("");
            ((TextView) MainActivity.this.findViewById(R.id.rbPeakRight)).setText("");

            controlTimerThread(false);

            stopService(new Intent(getBaseContext(), MyService.class));
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

        controlButtonState(1);

        if (hasCore()) {
            stopRecording(view);
            return;
        }

        if (!checkPermission()) {
            startLock.unlock();
            controlButtonState(0);

            Utils.requestPermissions(this);

            return;
        }

        if (Wrapper.getState() != Wrapper.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_native_components_not_ready, Toast.LENGTH_SHORT).show();
            startLock.unlock();
            controlButtonState(0);
            return;
        }

        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_check_connection, Toast.LENGTH_SHORT).show();
            startLock.unlock();
            controlButtonState(0);
            return;
        }

        if (!coolmic.isConnectionSet()) {
            startLock.unlock();
            controlButtonState(0);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Missing connection details");
            alertDialog.setMessage("You seem to have no connection details set. Should we load the Test System Data? If you decline please set them in the Settings.");
            alertDialog.setNegativeButton("No.", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Utils.loadCMTSData(MainActivity.this, "default");
                    startRecording(null);
                }
            });

            alertDialog.show();

            return;
        }

        if(coolmic.isCMTSConnection()) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle(R.string.coolmic_tos_title);
            alertDialog.setMessage(R.string.coolmic_tos);
            alertDialog.setNegativeButton(R.string.coolmic_tos_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startLock.unlock();
                    controlButtonState(0);
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
            int status = Wrapper.init(MainActivity.this, server, port_num, username, password, mountpoint, codec_string, Integer.parseInt(sampleRate_string), Integer.parseInt(channel_string), buffersize);

            if(status != 0)
            {
                throw new Exception("Failed to init Core: "+String.valueOf(status));
            }

            status = Wrapper.performMetaDataQualityUpdate(title, artist, Double.parseDouble(quality_string), 0);

            if(status != 0)
            {
                throw new Exception(getString(R.string.exception_failed_metadata_quality, status));
            }

            status = Wrapper.start();

            Log.d("VS", "Status:" + status);

            if(status != 0)
            {
                throw new Exception(getString(R.string.exception_start_failed, status));
            }

            int interval = Integer.parseInt(coolmic.getVuMeterInterval());

            Wrapper.setVuMeterInterval(interval);

            controlVuMeterUI(interval != 0);

            startLock.unlock();
        } catch (Exception e) {
            e.printStackTrace();

            Log.e("VS", "Livestream Start: Exception: ", e);

            stopRecording(null);

            Toast.makeText(MainActivity.this, R.string.exception_failed_start_general, Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRecording(@SuppressWarnings("unused") View view) {
        if(Wrapper.getState() != Wrapper.WrapperInitializationStatus.WRAPPER_INTITIALIZED) {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_toast_native_components_not_ready, Toast.LENGTH_SHORT).show();
        }

        if(!hasCore())
        {
            return;
        }

        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);

        controlRecordingUI(false);

        if(startLock.isHeldByCurrentThread()) {
            startLock.unlock();
        }

        Wrapper.stop();
        Wrapper.unref();

        Toast.makeText(MainActivity.this, R.string.broadcast_stop_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    @SuppressWarnings("unused")
    private void callbackHandler(int what, int arg0, int arg1)
    {
        Log.d("CBHandler", String.format("Handler VUMeter: %s Arg0: %d Arg1: %d ", String.valueOf(what), arg0, arg1));

        final int what_final = what;
        final int arg0_final = arg0;
        final int arg1_final = arg1;
        MainActivity.this.runOnUiThread(new Runnable(){
            public void run(){
                switch(what_final) {
                    case 1:
                        controlTimerThread(true, 0);

                        break;
                    case 2:
                        controlRecordingUI(false);

                        break;
                    case 3:
                        controlRecordingUI(false);

                        Toast.makeText(MainActivity.this, getString(R.string.mainactivity_callback_error, arg0_final), Toast.LENGTH_SHORT).show();

                        if(hasCore())
                        {
                            Wrapper.unref();
                        }

                        break;
                    case 4:
                        String error = "";

                        if(arg1_final != 0)
                        {
                            error = getString(R.string.txtStateFormatError, arg1_final);
                        }

                        if(arg0_final == 2)
                        {
                            controlRecordingUI(true);
                        }
                        else if(arg0_final == 4 || arg0_final == 5)
                        {
                            stopRecording(null);
                        }

                        ((TextView) MainActivity.this.findViewById(R.id.txtState)).setText(getString(R.string.txtStateFormat, Utils.getStringByName(MainActivity.this, "coolmic_cs", arg0_final), error));
                        //Toast.makeText(MainActivity.this, getString(R.string.mainactivity_callback_streamstate, arg0_final, arg1_final), Toast.LENGTH_SHORT).show();

                        break;
                }
            }
        });
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

    @SuppressWarnings("unused")
    private void callbackVUMeterHandler(VUMeterResult result)
    {
        Log.d("Handler VUMeter: ", String.valueOf(result.global_power));

        final VUMeterResult result_final = result;
        MainActivity.this.runOnUiThread(new Runnable(){
            public void run(){
                TextProgressBar pbVuMeterLeft = (TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft);
                TextProgressBar pbVuMeterRight = (TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight);

                TextView rbPeakLeft = (TextView) MainActivity.this.findViewById(R.id.rbPeakLeft);
                TextView rbPeakRight = (TextView) MainActivity.this.findViewById(R.id.rbPeakRight);

                if(result_final.channels < 2) {
                    pbVuMeterLeft.setProgress(normalizeVUMeterPower(result_final.global_power));
                    pbVuMeterLeft.setTextColor(result_final.global_power_color);
                    pbVuMeterLeft.setText(normalizeVUMeterPowerString(result_final.global_power));
                    pbVuMeterRight.setProgress(normalizeVUMeterPower(result_final.global_power));
                    pbVuMeterRight.setTextColor(result_final.global_power_color);
                    pbVuMeterRight.setText(normalizeVUMeterPowerString(result_final.global_power));
                    rbPeakLeft.setText(normalizeVUMeterPeak(result_final.global_peak));
                    rbPeakLeft.setTextColor(result_final.global_peak_color);
                    rbPeakRight.setText(normalizeVUMeterPeak(result_final.global_peak));
                    rbPeakRight.setTextColor(result_final.global_peak_color);
                }
                else
                {
                    pbVuMeterLeft.setProgress(normalizeVUMeterPower(result_final.channels_power[0]));
                    pbVuMeterLeft.setTextColor(result_final.channels_power_color[0]);
                    pbVuMeterLeft.setText(normalizeVUMeterPowerString(result_final.channels_power[0]));
                    pbVuMeterRight.setProgress(normalizeVUMeterPower(result_final.channels_power[1]));
                    pbVuMeterRight.setTextColor(result_final.channels_power_color[1]);
                    pbVuMeterRight.setText(normalizeVUMeterPowerString(result_final.channels_power[1]));
                    rbPeakLeft.setText(normalizeVUMeterPeak(result_final.channels_peak[0]));
                    rbPeakLeft.setTextColor(result_final.channels_peak_color[0]);
                    rbPeakRight.setText(normalizeVUMeterPeak(result_final.channels_peak[1]));
                    rbPeakRight.setTextColor(result_final.channels_peak_color[1]);
                }

            }
        });
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
