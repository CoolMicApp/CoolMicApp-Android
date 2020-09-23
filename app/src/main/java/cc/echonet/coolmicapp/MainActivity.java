/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2020
 *       on behalf of Jordan Erickson.
 *
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
 *
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
import androidx.annotation.NonNull;
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

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.Objects;

import cc.echonet.coolmicapp.BackgroundService.Client.Client;
import cc.echonet.coolmicapp.BackgroundService.Client.EventListener;
import cc.echonet.coolmicapp.BackgroundService.Constants;
import cc.echonet.coolmicapp.BackgroundService.Server.Server;
import cc.echonet.coolmicapp.BackgroundService.State;
import cc.echonet.coolmicapp.Configuration.DialogIdentifier;
import cc.echonet.coolmicapp.Configuration.Manager;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicdspjava.VUMeterResult;

/**
 * This activity demonstrates how to use JNI to encode and decode Ogg/Vorbis audio
 */
public class MainActivity extends Activity implements EventListener {
    private final Client backgroundServiceClient = new Client(this, this);
    private Constants.CONTROL_UI currentState;

    private State backgroundServiceState;

    private Profile profile = null;
    private Button start_button;
    private boolean start_button_debounce_active = false;
    private SeekBar gainLeft;
    private SeekBar gainRight;

    private final Animation animation = new AlphaAnimation(1, 0);
    private final TransitionDrawable transitionButton;

    private final Animation clipAnimation = new AlphaAnimation(1, 0);
    private final TransitionDrawable clipTransitionButton;
    private boolean isLive = true;

    private Drawable buttonColor;
    private ClipboardManager myClipboard;

    private boolean isRecording = false;

    public MainActivity() {
        super();

        ColorDrawable transitionColorGrey = new ColorDrawable(Color.parseColor("#66999999"));
        ColorDrawable transitionColorRed = new ColorDrawable(Color.RED);
        ColorDrawable[] transitionColorDefault = {transitionColorGrey, transitionColorRed};

        transitionButton = new TransitionDrawable(transitionColorDefault);
        clipTransitionButton = new TransitionDrawable(transitionColorDefault);
    }


    private void sendGain(int left, int right) {
        backgroundServiceClient.setGain(left, right);

        profile.getVolume().setLeft(left);
        profile.getVolume().setRight(right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        if (!new Manager(this).getGlobalConfiguration().getDeveloperMode()) {
            for (int i = 0; i < menu.size(); i++) {
                final @NotNull MenuItem item = menu.getItem(i);
                if (item.getOrder() >= 1000)
                    item.setVisible(false);
            }
        }

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
            /* developer mode below */
            case R.id.menu_action_devel_permission_check:
                Utils.requestPermissions(this, profile, true);
                return true;

            default:
                Toast.makeText(getApplicationContext(), R.string.menu_action_default, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void exitApp() {
        backgroundServiceClient.stopRecording();
        backgroundServiceClient.disconnect();
        isRecording = false;

        stopService(new Intent(this, Server.class));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopService(new Intent(getApplicationContext(), Server.class));
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
        final @NotNull Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void goAbout() {
        final @NotNull Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
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
        if (!isRecording){
            backgroundServiceClient.connect();
            controlRecordingUI(currentState);
        }
        controlVuMeterUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isRecording){
            // Unbind from the service
            backgroundServiceClient.disconnect();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        ImageView imageView1;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);

        imageView1 = findViewById(R.id.imageView1);

        Log.v("onCreate", (imageView1 == null ? "iv null" : "iv ok"));

        if (imageView1 != null) {
            imageView1.setOnClickListener(this::onImageClick);
        }

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);

        clipAnimation.setDuration(500); // duration - half a second
        clipAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        clipAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        clipAnimation.setRepeatMode(Animation.REVERSE);

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

        controlVuMeterUI();

        controlRecordingUI(currentState);

        start_button.setOnLongClickListener(view -> {
            backgroundServiceClient.reloadParameters();
            return true;
        });

        start_button.setOnClickListener(v -> {
            if (start_button_debounce_active)
                return;
            start_button_debounce_active = true;
            v.postDelayed(() -> start_button_debounce_active = false, 500);
            start_button.setClickable(false);
            startRecording();
        });


        findViewById(R.id.next_segment_button).setOnClickListener(view -> {
            Intent fc = new Intent(Intent.ACTION_GET_CONTENT);
            //noinspection HardcodedFileSeparator
            fc.setType("*/*");
            startActivityForResult(fc, Constants.NEXTSEGMENT_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.NEXTSEGMENT_REQUEST_CODE:
                    final @NotNull Uri uri = Objects.requireNonNull(data.getData());
                    backgroundServiceClient.nextSegment(uri.toString());
                    break;
            }
        }
    }

    private void onImageClick(View view) {
        if (profile.getServer().isSet()) {
            try {
                ClipData myClip = ClipData.newPlainText("text", profile.getServer().getStreamURL().toString());
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getApplicationContext(), R.string.mainactivity_broadcast_url_copied, Toast.LENGTH_SHORT).show();
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), R.string.mainactivity_broadcast_url_not_copied, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_connectiondetails_unset, Toast.LENGTH_SHORT).show();
        }
    }

    private void performShare() {
        if (profile.getServer().isSet()) {
            try {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, profile.getServer().getStreamURL().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.menu_action_share_title)));
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), R.string.mainactivity_broadcast_url_not_shared, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.mainactivity_connectiondetails_unset, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void controlRecordingUI(Constants.CONTROL_UI state) {
        Log.d("MainActivity", "controlRecordingUI: state = " + currentState + " -> " + state);

        if (state == currentState) {
            return;
        }

        switch (state) {
            case CONTROL_UI_CONNECTING:
                start_button.startAnimation(animation);
                start_button.setBackground(transitionButton);
                transitionButton.startTransition(5000);

                start_button.setText(R.string.cmdStartInitializing);

                findViewById(R.id.next_segment_button).setEnabled(false);
                break;
            case CONTROL_UI_CONNECTED:
                start_button.startAnimation(animation);
                start_button.setBackground(transitionButton);
                transitionButton.startTransition(5000);

                start_button.setText(R.string.broadcasting);

                findViewById(R.id.next_segment_button).setEnabled(Utils.checkRequiredPermissions(this, false));
                break;
            case CONTROL_UI_DISCONNECTED:
                start_button.clearAnimation();
                start_button.setBackground(buttonColor);
                start_button.setText(R.string.start_broadcast);

                ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setProgress(0);
                ((ProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setProgress(0);
                ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterLeft)).setText("");
                ((TextProgressBar) MainActivity.this.findViewById(R.id.pbVuMeterRight)).setText("");
                ((TextView) MainActivity.this.findViewById(R.id.rbPeakLeft)).setText("");
                ((TextView) MainActivity.this.findViewById(R.id.rbPeakRight)).setText("");

                findViewById(R.id.next_segment_button).setEnabled(false);
                break;
        }

        currentState = state;
    }

    private void controlVuMeterUI() {
        int visibility = View.INVISIBLE;

        if (profile != null && profile.getVUMeter().getInterval() != 0)
            visibility = View.VISIBLE;

        findViewById(R.id.pbVuMeterLeft).setVisibility(visibility);
        findViewById(R.id.pbVuMeterRight).setVisibility(visibility);
        findViewById(R.id.rbPeakLeft).setVisibility(visibility);
        findViewById(R.id.rbPeakRight).setVisibility(visibility);
    }

    private void startRecording() {
        /* cmtsTOSAccepted is always true as the user accepted it on load. */
        backgroundServiceClient.startRecording(true, profile.getName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!Utils.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (Utils.checkRequiredPermissions(this, false))
                startRecording();
        }
    }

    @Override
    public void onBackgroundServiceConnected() {
        profile = backgroundServiceClient.getProfile();
        controlVuMeterUI();

        new Dialog(DialogIdentifier.NEW_VERSION, this, profile).showIfNecessary();
        new Dialog(DialogIdentifier.FIRST_TIME, this, profile).showIfNecessary();
    }

    @Override
    public void onBackgroundServiceDisconnected() {
        /* NOOP */
    }

    @Override
    public void onBackgroundServiceState(State state) {
        Button clipButton = findViewById(R.id.next_segment_button);
        boolean isLive = state.isLive || state.uiState == Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED;

        backgroundServiceState = state;
        controlRecordingUI(state.uiState);

        ((TextView) findViewById(R.id.timerValue)).setText(state.getTimerString(this));
        ((TextView) findViewById(R.id.txtState)).setText(state.getTextState(this));
        ((TextView) findViewById(R.id.txtListeners)).setText(state.getListenersString(this));

        if (this.isLive != isLive) {
            this.isLive = isLive;

            if (isLive) {
                clipButton.clearAnimation();
                clipButton.setBackground(buttonColor);
                clipButton.setText(R.string.next_segment);
            } else {
                clipButton.startAnimation(clipAnimation);
                clipButton.setBackground(clipTransitionButton);
                clipTransitionButton.startTransition(5000);
                clipButton.setText(R.string.next_segment_active);
            }
        }
    }

    @Override
    public void onBackgroundServiceError() {
        // TODO...
    }

    @Override
    public void onBackgroundServiceStartRecording() {
        isRecording = true;
        controlVuMeterUI();
        start_button.setClickable(true);
    }

    @Override
    public void onBackgroundServiceStopRecording() {
        isRecording = false;
        start_button.setClickable(true);

        // Force reload of UI state:
        currentState = Constants.CONTROL_UI.CONTROL_UI_CONNECTED;
        controlRecordingUI(Constants.CONTROL_UI.CONTROL_UI_DISCONNECTED);
    }

    @Override
    public void onBackgroundServicePermissionsMissing() {
        Utils.requestPermissions(this, profile, false);
    }

    @Override
    public void onBackgroundServiceConnectionUnset() {
        AlertDialog.Builder alertDialog = Utils.buildAlertDialogCMTSTOS(this);
        alertDialog.setPositiveButton(R.string.mainactivity_missing_connection_details_yes, (dialog, which) -> {
            Utils.loadCMTSData(MainActivity.this, profile);
            startRecording();
        });
        alertDialog.show();
    }

    @Override
    public void onBackgroundServiceCMTSTOSAcceptMissing() {
        AlertDialog.Builder alertDialogCMTSTOS = new AlertDialog.Builder(this);
        alertDialogCMTSTOS.setTitle(R.string.coolmic_tos_title);
        alertDialogCMTSTOS.setMessage(R.string.coolmic_tos);
        alertDialogCMTSTOS.setNegativeButton(R.string.coolmic_tos_cancel, (dialog, which) -> dialog.cancel());
        alertDialogCMTSTOS.setPositiveButton(R.string.coolmic_tos_accept, (dialog, which) -> startRecording());

        alertDialogCMTSTOS.show();
    }

    @Override
    public void onBackgroundServiceVUMeterUpdate(VUMeterResult result) {
        TextProgressBar pbVuMeterLeft = findViewById(R.id.pbVuMeterLeft);
        TextProgressBar pbVuMeterRight = findViewById(R.id.pbVuMeterRight);

        TextView rbPeakLeft = findViewById(R.id.rbPeakLeft);
        TextView rbPeakRight = findViewById(R.id.rbPeakRight);

        if (result.channels < 2) {
            pbVuMeterLeft.setProgress(Utils.normalizeVUMeterPower(result.global_power));
            pbVuMeterLeft.setTextColor(result.global_power_color);
            pbVuMeterLeft.setText(Utils.VUMeterPowerToString(result.global_power));
            pbVuMeterRight.setProgress(Utils.normalizeVUMeterPower(result.global_power));
            pbVuMeterRight.setTextColor(result.global_power_color);
            pbVuMeterRight.setText(Utils.VUMeterPowerToString(result.global_power));
            rbPeakLeft.setText(Utils.VUMeterPeakToString(result.global_peak));
            rbPeakLeft.setTextColor(result.global_peak_color);
            rbPeakRight.setText(Utils.VUMeterPeakToString(result.global_peak));
            rbPeakRight.setTextColor(result.global_peak_color);
        } else {
            pbVuMeterLeft.setProgress(Utils.normalizeVUMeterPower(result.channels_power[0]));
            pbVuMeterLeft.setTextColor(result.channels_power_color[0]);
            pbVuMeterLeft.setText(Utils.VUMeterPowerToString(result.channels_power[0]));
            pbVuMeterRight.setProgress(Utils.normalizeVUMeterPower(result.channels_power[1]));
            pbVuMeterRight.setTextColor(result.channels_power_color[1]);
            pbVuMeterRight.setText(Utils.VUMeterPowerToString(result.channels_power[1]));
            rbPeakLeft.setText(Utils.VUMeterPeakToString(result.channels_peak[0]));
            rbPeakLeft.setTextColor(result.channels_peak_color[0]);
            rbPeakRight.setText(Utils.VUMeterPeakToString(result.channels_peak[1]));
            rbPeakRight.setTextColor(result.channels_peak_color[1]);
        }
    }

    @Override
    public void onBackgroundServiceGainUpdate(int left, int right) {
        gainLeft.setProgress(left);
        gainRight.setProgress(right);
    }
}
