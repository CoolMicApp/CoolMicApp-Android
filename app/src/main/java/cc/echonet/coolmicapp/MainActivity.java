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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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


    CoolMic coolmic = null;
    Button start_button;
    boolean start_button_debounce_active = false;
    SeekBar gainLeft;
    SeekBar gainRight;

    Animation animation = new AlphaAnimation(1, 0);

    ColorDrawable transitionColorGrey = new ColorDrawable(Color.parseColor("#66999999"));
    ColorDrawable transitionColorRed = new ColorDrawable(Color.RED);
    ColorDrawable[] transitionColorDefault = {transitionColorGrey, transitionColorRed};

    TransitionDrawable transitionButton = new TransitionDrawable(transitionColorDefault);

    Drawable buttonColor;
    ImageView imageView1;
    ClipboardManager myClipboard;


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
                sendStreamReload();
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
            Bundle bundle = msg.getData();

            switch (msg.what) {
                case Constants.S2C_MSG_STATE_REPLY:
                    activity.backgroundServiceState = (BackgroundServiceState) bundle.getSerializable("state");

                    if (activity.backgroundServiceState == null) {
                        return;
                    }

                    Log.v("IH", "In Handler: S2C_MSG_STATE_REPLY: State=" + activity.backgroundServiceState.uiState.toString());

                    activity.controlRecordingUI(activity.backgroundServiceState.uiState);

                    ((TextView) activity.findViewById(R.id.timerValue)).setText(activity.backgroundServiceState.timerString);
                    ((TextView) activity.findViewById(R.id.txtState)).setText(activity.backgroundServiceState.txtState);
                    ((TextView) activity.findViewById(R.id.txtListeners)).setText(activity.backgroundServiceState.listenersString);

                    if (activity.backgroundServiceState.uiState.equals(Constants.CONTROL_UI.CONTROL_UI_CONNECTING)) {
                        Log.v("IH", "In Handler: S2C_MSG_STATE_REPLY: X!");
                    }

                    break;

                case Constants.S2C_MSG_ERROR:
                    String error = bundle.getString("error");

                    Log.v("IH", "In Handler: S2C_MSG_ERROR: State=" + activity.backgroundServiceState.uiState.toString());
                    Log.v("IH", "In Handler: S2C_MSG_ERROR: error=" + error);

                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();

                    if (activity.backgroundServiceState.uiState.equals(Constants.CONTROL_UI.CONTROL_UI_CONNECTING)) {
                        Log.v("IH", "In Handler: S2C_MSG_ERROR: X!");
                    }

                    break;
                case Constants.S2C_MSG_STREAM_START_REPLY:
                    activity.controlVuMeterUI(Integer.parseInt(activity.coolmic.getVuMeterInterval()) != 0);

                    Log.v("IH", "In Handler: S2C_MSG_STREAM_START_REPLY: X!");
                    activity.start_button.setClickable(true);
                    break;

                case Constants.S2C_MSG_STREAM_STOP_REPLY:
                    boolean was_running = bundle.getBoolean("was_running");

                    Log.v("IH", "In Handler: S2C_MSG_STREAM_STOP_REPLY: X!");

                    if (was_running) {
                        Toast.makeText(activity, R.string.broadcast_stop_message, Toast.LENGTH_SHORT).show();
                    }

                    activity.start_button.setClickable(true);
                    break;

                case Constants.S2C_MSG_PERMISSIONS_MISSING:
                    Utils.requestPermissions(activity);

                    break;

                case Constants.S2C_MSG_CONNECTION_UNSET:
                    AlertDialog.Builder alertDialog = Utils.buildAlertDialogCMTSTOS(activity);
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

                    break;
                case Constants.C2S_MSG_GAIN:
                    Bundle bundleGain = msg.getData();

                    activity.setGain(bundleGain.getInt("left"), bundleGain.getInt("right"));

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void setGain(int left, int right) {
        gainLeft.setProgress(left);
        gainRight.setProgress(right);
    }

    private void sendGain(int left, int right) {
        if (mBackgroundServiceBound) {
            Message msgReply = Message.obtain(null, Constants.C2S_MSG_GAIN, 0, 0);

            msgReply.replyTo = mBackgroundServiceClient;

            Bundle bundle = msgReply.getData();

            bundle.putInt("left", left);
            bundle.putInt("right", right);

            try {
                mBackgroundService.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        coolmic.setVolumeLeft(left);
        coolmic.setVolumeRight(right);
    }

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
        stopRecording();

        disconnectService();

        stopService(new Intent(this, BackgroundService.class));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        }, 250);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 500);

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

        this.exitApp();
    }

    private void connectService() {
        if (!mBackgroundServiceBound) {
            Intent intent = new Intent(this, BackgroundService.class);
            startService(intent);
            bindService(intent, mBackgroundServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void disconnectService() {
        if (mBackgroundServiceBound) {
            unbindService(mBackgroundServiceConnection);
            mBackgroundServiceBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        connectService();
        controlRecordingUI(currentState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        disconnectService();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        Log.v("onCreate", (imageView1 == null ? "iv null" : "iv ok"));

        if (imageView1 != null) {
            imageView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onImageClick(v);
                }
            });
        }

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        start_button = findViewById(R.id.start_recording_button);

        gainLeft = findViewById(R.id.pbGainMeterLeft);
        gainRight = findViewById(R.id.pbGainMeterRight);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (backgroundServiceState.channels != 2) {
                        int value = seekBar.getProgress();

                        setGain(value, value);
                    }

                    MainActivity.this.sendGain(gainLeft.getProgress(), gainRight.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        gainLeft.setOnSeekBarChangeListener(seekBarChangeListener);
        gainRight.setOnSeekBarChangeListener(seekBarChangeListener);

        buttonColor = start_button.getBackground();

        coolmic = new CoolMic(this, "default");

        controlVuMeterUI(Integer.parseInt(coolmic.getVuMeterInterval()) != 0);

        controlRecordingUI(currentState);

        setGain(coolmic.getVolumeLeft(), coolmic.getVolumeRight());

        start_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                sendStreamReload();
                return true;
            }
        });

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_button_debounce_active)
                    return;
                start_button_debounce_active = true;
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        start_button_debounce_active = false;
                    }
                }, 500);
                start_button.setClickable(false);
                startRecording(v);
            }
        });
    }

    private void sendStreamReload() {
        if (mBackgroundServiceBound) {
            System.out.println("MainActivity.sendStreamReload: We have a background service!");
            Message msgReply = Message.obtain(null, Constants.C2S_MSG_STREAM_RELOAD, 0, 0);

            msgReply.replyTo = mBackgroundServiceClient;

            try {
                mBackgroundService.send(msgReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("MainActivity.sendStreamReload: No background service!");
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
    }

    private long getChannels() {
        if (currentState == Constants.CONTROL_UI.CONTROL_UI_CONNECTED) {
            return backgroundServiceState.channels;
        }

        if (coolmic != null) {
            return Integer.parseInt(coolmic.getChannels());
        }

        return 2; // default.
    }

    public void controlRecordingUI(Constants.CONTROL_UI state) {
        View sb;

        sb = findViewById(R.id.pbGainMeterLeft);
        if (this.getChannels() == 2) {
            sb.setVisibility(View.VISIBLE);
        } else {
            sb.setVisibility(View.GONE);
        }

        if (state == currentState) {
            return;
        }

        if (state == Constants.CONTROL_UI.CONTROL_UI_CONNECTING) {
            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);
            transitionButton.startTransition(5000);

            start_button.setText(R.string.cmdStartInitializing);
        } else if (state == Constants.CONTROL_UI.CONTROL_UI_CONNECTED) {
            start_button.startAnimation(animation);
            start_button.setBackground(transitionButton);
            transitionButton.startTransition(5000);

            start_button.setText(R.string.broadcasting);
        } else {
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

    public void controlVuMeterUI(boolean visible) {
        View meter;
        int visibility = visible ? View.VISIBLE : View.GONE;

        meter = findViewById(R.id.llVuMeterLeft);
        if (meter != null) meter.setVisibility(visibility);

        meter = findViewById(R.id.llVuMeterRight);
        if (meter != null) meter.setVisibility(visibility);

        findViewById(R.id.pbVuMeterLeft).setVisibility(visibility);
        findViewById(R.id.pbVuMeterRight).setVisibility(visibility);
        findViewById(R.id.rbPeakLeft).setVisibility(visibility);
        findViewById(R.id.rbPeakRight).setVisibility(visibility);
    }

    public void startRecording(View view) {
        startRecording(view, true);
    }

    public void startRecording(View view, boolean cmtsTOSAccepted) {
        if (mBackgroundServiceBound) {
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

    public void stopRecording() {
        if (mBackgroundServiceBound) {
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
        if (!Utils.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            startRecording(null);
        }
    }


    static int normalizeVUMeterPower(double power) {
        int g_p = (int) ((60. + power) * (100. / 60.));

        if (g_p > 100) {
            g_p = 100;
        }

        if (g_p < 0) {
            g_p = 0;
        }

        return g_p;
    }

    static String normalizeVUMeterPeak(int peak) {
        if (peak == -32768 || peak == 32767) {
            return "P";
        } else if (peak < -30000 || peak > 30000) {
            return "p";
        } else if (peak < -8000 || peak > 8000) {
            return "g";
        } else {
            return "";
        }
    }

    static String normalizeVUMeterPowerString(double power) {
        if (power < -100) {
            return "-100";
        } else if (power > 0) {
            return "0";
        } else {
            return String.format(Locale.ENGLISH, "%.2f", power);
        }
    }

    public void handleVUMeterResult(VUMeterResult vuMeterResult) {
        TextProgressBar pbVuMeterLeft = (TextProgressBar) this.findViewById(R.id.pbVuMeterLeft);
        TextProgressBar pbVuMeterRight = (TextProgressBar) this.findViewById(R.id.pbVuMeterRight);

        TextView rbPeakLeft = (TextView) this.findViewById(R.id.rbPeakLeft);
        TextView rbPeakRight = (TextView) this.findViewById(R.id.rbPeakRight);

        if (vuMeterResult.channels < 2) {
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
        } else {
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
