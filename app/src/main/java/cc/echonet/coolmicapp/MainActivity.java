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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

import cc.echonet.coolmicdspjava.VUMeterResult;

/**
 * This activity demonstrates how to use JNI to encode and decode ogg/vorbis audio
 */
public class MainActivity extends Activity {
    Messenger mBackgroundService = null;
    Messenger mBackgroundServiceClient = new Messenger(new IncomingHandler(this));
    boolean mBackgroundServiceBound = false;
    Constants.CONTROL_UI currentState;

    BackgroundServiceState backgroundServiceState;


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
                    activity.backgroundServiceState = (BackgroundServiceState) bundle.getSerializable("state");

                    if(activity.backgroundServiceState == null) {
                        return;
                    }

                    activity.controlRecordingUI(activity.backgroundServiceState.uiState);

                    ((TextView) activity.findViewById(R.id.timerValue)).setText(activity.backgroundServiceState.timerString);
                    ((TextView) activity.findViewById(R.id.txtState)).setText(activity.backgroundServiceState.txtState);
                    ((TextView) activity.findViewById(R.id.txtListeners)).setText(activity.backgroundServiceState.listenersString);

                    break;

                case Constants.S2C_MSG_STREAM_START_REPLY:
                    activity.controlVuMeterUI(Integer.parseInt(activity.coolmic.getVuMeterInterval()) != 0);

                    break;

                case Constants.S2C_MSG_STREAM_STOP_REPLY:
                    Toast.makeText(activity, R.string.broadcast_stop_message, Toast.LENGTH_SHORT).show();

                    break;

                case Constants.S2C_MSG_PERMISSIONS_MISSING:
                    Utils.requestPermissions(activity);

                    break;

                case Constants.S2C_MSG_CONNECTION_UNSET:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                    alertDialog.setTitle(R.string.mainactivity_missing_connection_details_title);
                    alertDialog.setMessage(R.string.mainactivity_missing_connection_details_body);
                    alertDialog.setNegativeButton(R.string.mainactivity_missing_connection_details_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.setPositiveButton(R.string.mainactivity_missing_connection_details_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.loadCMTSData(activity, "default");
                            activity.startRecording(null);
                        }
                    });

                    alertDialog.show();

                    break;

                case Constants.S2C_MSG_CMTS_TOS:
                    AlertDialog.Builder alertDialogCMTSTOS = new AlertDialog.Builder(activity);
                    alertDialogCMTSTOS.setTitle(R.string.coolmic_tos_title);
                    alertDialogCMTSTOS.setMessage(R.string.coolmic_tos);
                    alertDialogCMTSTOS.setNegativeButton(R.string.coolmic_tos_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialogCMTSTOS.setPositiveButton(R.string.coolmic_tos_accept, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            activity.startRecording(null, true);
                        }
                    });

                    alertDialogCMTSTOS.show();

                    break;
                case Constants.S2C_MSG_VUMETER:
                    Bundle bundleVUMeter = msg.getData();

                    activity.handleVUMeterResult((VUMeterResult) bundleVUMeter.getSerializable("vumeterResult"));
                default:
                    super.handleMessage(msg);
            }
        }
    }

    CoolMic coolmic = null;
    Button start_button;

    Animation animation = new AlphaAnimation(1, 0);

    ColorDrawable transitionColorGrey = new ColorDrawable(Color.parseColor("#66999999"));
    ColorDrawable transitionColorRed = new ColorDrawable(Color.RED);
    ColorDrawable[] transitionColorDefault = {transitionColorGrey, transitionColorRed};

    TransitionDrawable transitionButton = new TransitionDrawable(transitionColorDefault);

    Drawable buttonColor;
    ImageView imageView1;
    ClipboardManager myClipboard;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
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
        stopService(new Intent(this, BackgroundService.class));
    }

    private void goSettings() {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    private void goAbout() {
        Intent i = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(i);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageView1.getLayoutParams().height = 60;
        } else {
            imageView1.getLayoutParams().height = 400;
        }
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
        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);
        bindService(intent, mBackgroundServiceConnection,
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

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        Log.v("onCreate", (imageView1 == null ? "iv null" : "iv ok"));

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        start_button = (Button) findViewById(R.id.start_recording_button);
        buttonColor = start_button.getBackground();

        coolmic = new CoolMic(this, "default");

        controlVuMeterUI(Integer.parseInt(coolmic.getVuMeterInterval()) != 0);

        controlRecordingUI(currentState);

        start_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mBackgroundServiceBound)
                {
                    Message msgReply = Message.obtain(null, Constants.C2S_MSG_STREAM_RELOAD, 0, 0);

                    msgReply.replyTo = mBackgroundServiceClient;

                    try {
                        mBackgroundService.send(msgReply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });
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
        if(state == currentState)
        {
            return;
        }

        if(state == Constants.CONTROL_UI.CONTROL_UI_CONNECTING)
        {
            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);
            transitionButton.startTransition(5000);

            start_button.setText(R.string.cmdStartInitializing);
        }
        else if(state == Constants.CONTROL_UI.CONTROL_UI_CONNECTED)
        {
            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);
            transitionButton.startTransition(5000);

            start_button.setText(R.string.broadcasting);
        }
        else
        {
            start_button.clearAnimation();
            start_button.setBackground(buttonColor);
            start_button.setText(R.string.start_broadcast);

            ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setProgress(0);
            ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setProgress(0);
            ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setText("");
            ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setText("");
            ((TextView) MainActivity.this.findViewById(R.id.rbPeakLeft)).setText("");
            ((TextView) MainActivity.this.findViewById(R.id.rbPeakRight)).setText("");
        }

        currentState = state;
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
        startRecording(view, false);
    }

    public void startRecording(View view, boolean cmtsTOSAccepted) {
        if(mBackgroundServiceBound)
        {
            Message msgReply = Message.obtain(null, Constants.C2S_MSG_STREAM_ACTION, 0, 0);

            msgReply.replyTo = mBackgroundServiceClient;

            Bundle bundle = msgReply.getData();

            bundle.putString("profile", "default");
            bundle.putBoolean("cmtsTOSAccepted", cmtsTOSAccepted);

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
            startRecording(null);
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
        TextProgressBar pbVuMeterLeft = (TextProgressBar) this.findViewById(R.id.pbVuMeterLeft);
        TextProgressBar pbVuMeterRight = (TextProgressBar) this.findViewById(R.id.pbVuMeterRight);

        TextView rbPeakLeft = (TextView) this.findViewById(R.id.rbPeakLeft);
        TextView rbPeakRight = (TextView) this.findViewById(R.id.rbPeakRight);

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
}
