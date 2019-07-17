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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import cc.echonet.coolmicapp.BackgroundServiceInterface.State;
import cc.echonet.coolmicapp.BackgroundServiceInterface.Client.Client;
import cc.echonet.coolmicapp.BackgroundServiceInterface.Client.EventListener;
import cc.echonet.coolmicdspjava.VUMeterResult;
import cc.echonet.coolmicapp.BackgroundServiceInterface.Constants;

/**
 * This activity demonstrates how to use JNI to encode and decode ogg/vorbis audio
 */
public class MainActivity extends Activity implements EventListener {
    private Client backgroundServiceClient = new Client(this, this);
    Constants.CONTROL_UI currentState;

    State backgroundServiceState;

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



    private void sendGain(int left, int right) {
        backgroundServiceClient.setGain(left, right);

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
        backgroundServiceClient.stopRecording();
        backgroundServiceClient.disconnect();

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
    public void onDestroy() {
        super.onDestroy();

        Log.v("$$$$$$", "In Method: onDestroy()");

        this.exitApp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        backgroundServiceClient.connect();
        controlRecordingUI(currentState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        backgroundServiceClient.disconnect();
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
                    if (backgroundServiceState != null && backgroundServiceState.channels != 2) {
                        int value = seekBar.getProgress();

                        onBackgroundServiceGainUpdate(value, value);
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

        onBackgroundServiceGainUpdate(coolmic.getVolumeLeft(), coolmic.getVolumeRight());

        start_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                backgroundServiceClient.reloadParameters();
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
        if (currentState == Constants.CONTROL_UI.CONTROL_UI_CONNECTED && backgroundServiceState != null) {
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
        backgroundServiceClient.startRecording(cmtsTOSAccepted);
    }

    public void stopRecording() {
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

    @Override
    public void onBackgroundServiceState(State state) {
        backgroundServiceState = state;
        controlRecordingUI(state.uiState);

        ((TextView) findViewById(R.id.timerValue)).setText(state.timerString);
        ((TextView) findViewById(R.id.txtState)).setText(state.txtState);
        ((TextView) findViewById(R.id.txtListeners)).setText(state.listenersString);
    }

    @Override
    public void onBackgroundServiceError() {
        // TODO...
    }

    @Override
    public void onBackgroundServiceStartRecording() {
        controlVuMeterUI(Integer.parseInt(coolmic.getVuMeterInterval()) != 0);
        start_button.setClickable(true);
    }

    @Override
    public void onBackgroundServiceStopRecording() {
        start_button.setClickable(true);
    }

    @Override
    public void onBackgroundServicePermissionsMissing() {
        Utils.requestPermissions(this);
    }

    @Override
    public void onBackgroundServiceConnectionUnset() {
        AlertDialog.Builder alertDialog = Utils.buildAlertDialogCMTSTOS(this);
        alertDialog.setPositiveButton(R.string.mainactivity_missing_connection_details_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Utils.loadCMTSData(MainActivity.this, "default");
                startRecording(null);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onBackgroundServiceCMTSTOSAcceptMissing() {
        AlertDialog.Builder alertDialogCMTSTOS = new AlertDialog.Builder(this);
        alertDialogCMTSTOS.setTitle(R.string.coolmic_tos_title);
        alertDialogCMTSTOS.setMessage(R.string.coolmic_tos);
        alertDialogCMTSTOS.setNegativeButton(R.string.coolmic_tos_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogCMTSTOS.setPositiveButton(R.string.coolmic_tos_accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startRecording(null, true);
            }
        });

        alertDialogCMTSTOS.show();
    }

    @Override
    public void onBackgroundServiceVUMeterUpdate(VUMeterResult result) {
        TextProgressBar pbVuMeterLeft = findViewById(R.id.pbVuMeterLeft);
        TextProgressBar pbVuMeterRight = findViewById(R.id.pbVuMeterRight);

        TextView rbPeakLeft = findViewById(R.id.rbPeakLeft);
        TextView rbPeakRight = findViewById(R.id.rbPeakRight);

        if (result.channels < 2) {
            pbVuMeterLeft.setProgress(normalizeVUMeterPower(result.global_power));
            pbVuMeterLeft.setTextColor(result.global_power_color);
            pbVuMeterLeft.setText(normalizeVUMeterPowerString(result.global_power));
            pbVuMeterRight.setProgress(normalizeVUMeterPower(result.global_power));
            pbVuMeterRight.setTextColor(result.global_power_color);
            pbVuMeterRight.setText(normalizeVUMeterPowerString(result.global_power));
            rbPeakLeft.setText(normalizeVUMeterPeak(result.global_peak));
            rbPeakLeft.setTextColor(result.global_peak_color);
            rbPeakRight.setText(normalizeVUMeterPeak(result.global_peak));
            rbPeakRight.setTextColor(result.global_peak_color);
        } else {
            pbVuMeterLeft.setProgress(normalizeVUMeterPower(result.channels_power[0]));
            pbVuMeterLeft.setTextColor(result.channels_power_color[0]);
            pbVuMeterLeft.setText(normalizeVUMeterPowerString(result.channels_power[0]));
            pbVuMeterRight.setProgress(normalizeVUMeterPower(result.channels_power[1]));
            pbVuMeterRight.setTextColor(result.channels_power_color[1]);
            pbVuMeterRight.setText(normalizeVUMeterPowerString(result.channels_power[1]));
            rbPeakLeft.setText(normalizeVUMeterPeak(result.channels_peak[0]));
            rbPeakLeft.setTextColor(result.channels_peak_color[0]);
            rbPeakRight.setText(normalizeVUMeterPeak(result.channels_peak[1]));
            rbPeakRight.setTextColor(result.channels_peak_color[1]);
        }
    }

    @Override
    public void onBackgroundServiceGainUpdate(int left, int right) {
        gainLeft.setProgress(left);
        gainRight.setProgress(right);
    }
}
