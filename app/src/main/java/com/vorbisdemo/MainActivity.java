package com.vorbisdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.PrintWriter;

import cc.echonet.coolmicdspjava.Wrapper;

/**
 * This activity demonstrates how to use JNI to encode and decode ogg/vorbis audio
 */
public class MainActivity extends Activity {
    public static final String TIMER_PER = "00:00:00";
    /**
     * Logging tag
     */
    private static final String TAG = "MainActivity";
    private static final int LED_NOTIFICATION_ID = 0;
    final Context context = this;
    Thread streamThread;
    boolean isThreadOn = false;
    DataOutputStream dos;
    String hostname = "giss.tv";
    PrintWriter output = null;
    BufferedReader reader = null;
    //Setting newSetting;
    // CoolMicSetting newSetting;
    DatabaseHandler db;
    CoolMic coolmic;
    Button start_button;
    Button stop_button;
    Animation animation = new AlphaAnimation(1, 0);
    ColorDrawable gray_color = new ColorDrawable(Color.parseColor("#66999999"));
    ColorDrawable[] color = {gray_color, new ColorDrawable(Color.RED)};
    TransitionDrawable trans = new TransitionDrawable(color);
    Drawable buttonColor;
    ImageView imageView1;
    Menu myMenu;
    boolean backyes = false;
    ClipboardManager myClipboard;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    SharedPreferences sharedpreferences;

    /**
     * Text view to show logged messages
     */
    private TextView logArea;
    private int port = 8000;
    private ClipData myClip;
    //variable declaration for timer starts here
    private long startTime = 0L;
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
                    int milliseconds = (int) (updatedTime % 1000);
                    timerValue.setText("" + String.format("%02d", hours) + ":"
                            + String.format("%02d", mins) + ":"
                            + String.format("%02d", secs));
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
        if (isThreadOn) {
            menu.findItem(R.id.server_settings).setVisible(false);
            menu.findItem(R.id.audio_settings).setVisible(false);
            menu.findItem(R.id.general_setting).setVisible(false);
        } else {
            menu.findItem(R.id.server_settings).setVisible(true);
            menu.findItem(R.id.audio_settings).setVisible(true);
            menu.findItem(R.id.general_setting).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.home).setVisible(false);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Editor editor = sharedpreferences.edit();
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.home:
                editor.putString("TIMER_PER", "");
                editor.commit();
                goHome();
                return true;
            // action with ID action_settings was selected
            case R.id.general_setting:
                editor.putString("TIMER_PER", "");
                editor.commit();
                generalSetting();
                return true;
            case R.id.server_settings:
                editor.putString("TIMER_PER", "");
                editor.commit();
                serverSetting();
                return true;
            case R.id.audio_settings:
                editor.putString("TIMER_PER", "");
                editor.commit();
                audioSetting();
                return true;
            case R.id.help:
                editor.putString("TIMER_PER", "");
                editor.commit();
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://coolmic.net/help/"));
                startActivity(helpIntent);
                return true;
            case R.id.quite_app:
                editor.putString("TIMER_PER", "");
                editor.commit();
                exitApp();
                return true;
            default:
                Toast.makeText(getApplicationContext(), "Default Pressed !", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    private void exitApp() {
        ClearLED();
        Wrapper.stop();
        finish();
        System.exit(0);
    }

    private void goHome() {
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void generalSetting() {
        Intent i = new Intent(MainActivity.this, general.class);
        startActivity(i);
        finish();
    }

    private void audioSetting() {
        Intent i = new Intent(MainActivity.this, audio.class);
        startActivity(i);
        finish();
    }

    private void serverSetting() {
        Intent i = new Intent(MainActivity.this, server.class);
        startActivity(i);
        finish();
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

    private void RedFlashLight() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification();
        notif.ledARGB = 0xFFff0000;
        notif.flags = Notification.FLAG_SHOW_LIGHTS;
        notif.ledOnMS = 100;
        notif.ledOffMS = 100;
        nm.notify(LED_NOTIFICATION_ID, notif);
    }

    private void ClearLED() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(LED_NOTIFICATION_ID);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isThreadOn) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notif = new Notification();
            notif.ledARGB = 0xFFff0000;
            notif.flags = Notification.FLAG_SHOW_LIGHTS;
            notif.ledOnMS = 100;
            notif.ledOffMS = 100;
            nm.notify(LED_NOTIFICATION_ID, notif);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("$$$$$$", "In Method: onDestroy()");
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(LED_NOTIFICATION_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);
        timerValue = (TextView) findViewById(R.id.timerValue);
        BroadcastReceiver mPowerKeyReceiver = null;
        final IntentFilter theFilter = new IntentFilter();
        /** System Defined Broadcast */
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction(Intent.ACTION_USER_PRESENT);
        sharedpreferences = getSharedPreferences(TIMER_PER, Context.MODE_PRIVATE);
        if (!sharedpreferences.getString("TIMER_PER", "").equals("")) {
            timerValue.setText(sharedpreferences.getString("TIMER_PER", ""));
        }
        mPowerKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String strAction = intent.getAction();
                if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON) || strAction.equals(Intent.ACTION_USER_PRESENT)) {
                    if (isThreadOn) {
                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notif = new Notification();
                        notif.ledARGB = 0xFFff0000;
                        notif.flags = Notification.FLAG_SHOW_LIGHTS;
                        notif.ledOnMS = 100;
                        notif.ledOffMS = 100;
                        nm.notify(LED_NOTIFICATION_ID, notif);
                    }
                }
            }
        };
        getApplicationContext().registerReceiver(mPowerKeyReceiver, theFilter);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            imageView1.getLayoutParams().height = 400;
        } else {
            imageView1.getLayoutParams().height = 180;
        }

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        start_button = (Button) findViewById(R.id.start_recording_button);
        stop_button = (Button) findViewById(R.id.stop_recording_button);
        buttonColor = (Drawable) start_button.getBackground();
        logArea = (TextView) findViewById(R.id.log_area);
        logArea.setMovementMethod(new ScrollingMovementMethod());
        setLoggingHandlers();

        db = new DatabaseHandler(this);
        if (db.getCoolMicSettingCount() >= 1) {
            coolmic = db.getCoolMicDetails(1);
            String sr = coolmic.getSampleRate();
            coolmic.setSampleRate("8000");
            db.updateCoolMicDetails(coolmic);

            coolmic = db.getCoolMicDetails(1);
            coolmic.setSampleRate("11025");
            db.updateCoolMicDetails(coolmic);

            coolmic = db.getCoolMicDetails(1);
            coolmic.setSampleRate(sr);

            db.updateCoolMicDetails(coolmic);
            coolmic = db.getCoolMicDetails(1);
        } else {
            db.addCoolMicSetting(new CoolMic(1, "", "", "", "", "", "", "", "44100", "1", "-0.1", "false"));
            CoolMic cm = db.getCoolMicDetails(1);
            String log = "Id: " + cm.getID() + " ,title: " + cm.getTitle() + " ,generalUsername: " + cm.getGeneralUsername() + ", servername: " + cm.getServerName() + " , mountpoint: " + cm.getMountpoint() + ", username: " + cm.getUsername() + ", password: " + cm.getPassword() + ", sampleRate: " + cm.getSampleRate() + ", channels: " + cm.getChannels() + ", quality: " + cm.getQuality() + ", termCondition: " + cm.getTermCondition();
            Log.d("VS", log);
        }


    }

    public void onImageClick(View view) {

        try {
            String portnum = "";
            coolmic = db.getCoolMicDetails(1);
            String server = coolmic.getServerName();
            Integer port_num = 8000;
            int counter = 0;
            for (int i = 0; i < server.length(); i++) {
                if (server.charAt(i) == ':') {
                    counter++;
                }
            }
            if (counter == 1) {
                if (server.indexOf("/") > 0) {
                    String[] split = server.split(":");
                    server = split[0].concat(":").concat(split[1]);
                    portnum = "8000";
                    port_num = Integer.parseInt(portnum);
                } else {
                    String[] split = server.split(":");
                    server = split[0];
                    portnum = split[1];
                    port_num = Integer.parseInt(portnum);
                }
            } else if (counter == 2) {
                String[] split = server.split(":");
                server = split[0].concat(":").concat(split[1]);
                portnum = split[2];
                port_num = Integer.parseInt(portnum);
            }
            Log.d("VS", server);
            Log.d("VS", portnum);
            if (server != null && !server.isEmpty()) {
                String text = server + ":" + port_num.toString() + "/" + coolmic.getMountpoint();
                myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getApplicationContext(), "Broadcast URL copied to clipboard!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Set the connection details", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("VS", "Excpetion", e);
        }

    }

    private void setLoggingHandlers() {

    }

    @Override
    public void onBackPressed() {
        // Write your code here
        if (isThreadOn) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Stop Broadcasting?");
            alertDialog.setMessage("Tap [ Ok ] to stop broadcasting.");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    backyes = false;
                    dialog.cancel();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    backyes = true;
                    dialog.cancel();
                    invalidateOptionsMenu();
                    start_button.clearAnimation();
                    start_button.setBackground(buttonColor);
                    start_button.setText("Start Broadcast");

                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alertDialog.show();
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void startRecording(View view) {

        if (isOnline()) {
            if (coolmic.isConnectionSet()) {
                if (Boolean.valueOf((coolmic.getTermCondition()))) {
                    invalidateOptionsMenu();
                    isThreadOn = true;
                    //screenreceiver.setThreadStatus(true);
                    startService(new Intent(getBaseContext(), MyService.class));
                    RedFlashLight();
                    start_button.startAnimation(animation);
                    start_button.setBackground(trans);
                    trans.startTransition(5000);
                    start_button.setText("Broadcasting");
                    streamThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (isThreadOn) {
                                try {
                                    String portnum = "";
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
                                    Log.d("VS", server + " " + server + " " + port_num.toString() + " " + username + " " + password + "\n " + mountpoint + "" +
                                            " " + sampleRate_string + " " + channel_string + " " + quality_string + " " + title);

                                    Wrapper.init("audio/ogg; codec=vorbis", Integer.parseInt(sampleRate_string), Integer.parseInt(channel_string));
                                    Log.d("VS", "Status:" + Wrapper.start());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("VS", "IOException", e);
                                }
                            }

                        }

                    });
                    streamThread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "Accept the Term and Conditions !", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Set the connection details !", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Check Internet Connection !", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("deprecation")
    public void stopRecording(@SuppressWarnings("unused") View view) {
        //code to stop timer starts here
        Log.d("shared", sharedpreferences.getString("TIMER_PER", "") + "1111");
        Log.d("shared123", "shared pref not disp");
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
        //code to stop timer starts here
        ClearLED();
        invalidateOptionsMenu();
        start_button.clearAnimation();
        start_button.setBackground(buttonColor);
        start_button.setText("Start Broadcast");
        stopService(new Intent(getBaseContext(), MyService.class));

        Wrapper.stop();

        Editor editor = sharedpreferences.edit();
        editor.putString("TIMER_PER", "");
        editor.putString("TIMER_PER", (String) timerValue.getText());
        editor.commit();
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void logMessage(String msg) {
        logArea.append(msg + "\n");
        final int scrollAmount = logArea.getLayout().getLineTop(logArea.getLineCount())
                - logArea.getHeight();
        if (scrollAmount > 0)
            logArea.scrollTo(0, scrollAmount);
        else
            logArea.scrollTo(0, 0);
    }
    //code for displaying timer ends here
}
