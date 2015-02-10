package com.vorbisdemo;

import java.io.BufferedOutputStream; 
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.xiph.vorbis.player.VorbisPlayer;
import org.xiph.vorbis.recorder.VorbisRecorder;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity demonstrates how to use JNI to encode and decode ogg/vorbis audio
 */
public class MainActivity extends Activity {
    /**
     * Logging tag
     */
    private static final String TAG = "MainActivity";

    /**
     * The vorbis player
     */
    private VorbisPlayer vorbisPlayer;
    /**
     * The vorbis recorder
     */
    private VorbisRecorder vorbisRecorder;
    /**
     * The sample rate selection spinner
     */
    private Spinner sampleRateSpinner;
    /**
     * The channel config spinner
     */
    private Spinner chanelConfigSpinner;
    /**
     * Qualities layout to hide when bitrate radio option is selected
     */
    private LinearLayout availableQualitiesLayout;
    /**
     * The quality spinner when with quality radio option is selected
     */
    private Spinner qualitySpinner;
    /**
     * Bitrate layout to hide when quality radio option is selected
     */
    private LinearLayout availableBitratesLayout;
    /**
     * The bitrate spinner when bitrate radio option is selected
     */
    private Spinner bitrateSpinner;
    /**
     * Radio group for encoding types
     */
    private RadioGroup encodingTypeRadioGroup;
    /**
     * Recording handler for callbacks
     */
    private Handler recordingHandler;
    /**
     * Playback handler for callbacks
     */
    private Handler playbackHandler;
    /**
     * Text view to show logged messages
     */
    private TextView logArea;
    Thread streamThread;
    boolean isThreadOn=false;
    DataOutputStream dos;
    String hostname = "giss.tv";
    OutputStream out;
    PrintWriter output = null;
    private int port = 8000;
    BufferedReader reader = null;
    Socket s = null;
    //Setting newSetting;
   // CoolMicSetting newSetting;
    DatabaseHandler db;
    CoolMic coolmic;
    private TextView log;
    final Context context = this;
    Button start_button;
    Button stop_button;
    Animation animation = new AlphaAnimation(1, 0);
    ColorDrawable gray_color = new ColorDrawable(Color.parseColor("#66999999"));
    ColorDrawable[] color = {gray_color, new ColorDrawable(Color.RED)};
    TransitionDrawable trans = new TransitionDrawable(color);
	Drawable buttonColor;
	ImageView    imageView1 ;
	Menu myMenu;
	boolean backyes=false;
	
	 @Override
	 public boolean onPrepareOptionsMenu(Menu menu){
	     super.onPrepareOptionsMenu(menu);
	     if(isThreadOn){
	    	  menu.findItem(R.id.server_settings).setVisible(false);
	    	  menu.findItem(R.id.audio_settings).setVisible(false);
	    	  menu.findItem(R.id.general_setting).setVisible(false);
	     }else{
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
      switch (item.getItemId()) {
      // action with ID action_refresh was selected
      case R.id.home:
    	  goHome();
    	  return true;
      // action with ID action_settings was selected
      case R.id.general_setting:
    	  generalSetting();
    	  return true;
      case R.id.server_settings:
    	  serverSetting();
    	  return true;
      case R.id.audio_settings:
    	  audioSetting();
    	  return true;
      default:
    	  Toast.makeText(getApplicationContext(), "Default Pressed !", Toast.LENGTH_LONG).show();
        break;
      }
      return true;
    }
	private void goHome() {
		Intent i = new Intent(MainActivity.this, MainActivity.class);
		startActivity(i);
	}
	private void generalSetting() {
		Intent i = new Intent(MainActivity.this, general.class);
		startActivity(i);
	}
	
	private void audioSetting() {
		Intent i = new Intent(MainActivity.this, audio.class);
		startActivity(i);
	}
	private void serverSetting() {
		Intent i = new Intent(MainActivity.this, server.class);
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
	  if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
		  imageView1.getLayoutParams().height = 180;
		}else{
			  imageView1.getLayoutParams().height = 400;
		}
	 }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
		if (getResources().getConfiguration().orientation ==
		   Configuration.ORIENTATION_PORTRAIT) {
			  imageView1.getLayoutParams().height = 400;
       }else {
    	   imageView1.getLayoutParams().height = 180;
       }
		
		

      
	    animation.setDuration(500); // duration - half a second
	    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
	    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
	    animation.setRepeatMode(Animation.REVERSE);
	    start_button = (Button) findViewById(R.id.start_recording_button);
	    stop_button = (Button) findViewById(R.id.stop_recording_button);
	    buttonColor= (Drawable) start_button.getBackground();
        logArea = (TextView) findViewById(R.id.log_area);
        logArea.setMovementMethod(new ScrollingMovementMethod());
        setLoggingHandlers();
        db = new DatabaseHandler(this);
        if(db.getCoolMicSettingCount() >= 1){
   		    coolmic=db.getCoolMicDetails(1);
   		    String sr=coolmic.getSampleRate();
   		    coolmic.setSampleRate("8000");
   		    db.updateCoolMicDetails(coolmic);
   		 
   		    coolmic=db.getCoolMicDetails(1);
   		    coolmic.setSampleRate("11025");
   		    db.updateCoolMicDetails(coolmic);
   		 
   		    coolmic=db.getCoolMicDetails(1);
   		    coolmic.setSampleRate(sr);;
   		    db.updateCoolMicDetails(coolmic);
        	coolmic=db.getCoolMicDetails(1);
       }else{
    	   db.addCoolMicSetting(new CoolMic(1,"", "", "", "","", "", "44100", "1", "-0.1", "false"));
    	     CoolMic cm=db.getCoolMicDetails(1);
    	        String log = "Id: "+cm.getID()+" ,title: " + cm.getTitle() + " ,generalUsername: " + cm.getGeneralUsername()+", servername: "+cm.getServerName()+" , mountpoint: "+cm.getMountpoint()+", username: "+cm.getUsername()+", password: "+cm.getPassword()+", sampleRate: "+cm.getSampleRate()+", channels: "+cm.getChannels()	+", quality: "+cm.getQuality()+", termCondition: "+cm.getTermCondition();
    	        Log.d("VS", log);
       }
 	   if (vorbisRecorder != null && vorbisRecorder.isRecording()) {
 			start_button.startAnimation(animation);
			start_button.setBackground(trans);
			trans.startTransition(5000);
			start_button.setText("Broadcasting");
       }
    }
    public void generateNoteOnSD(String sFileName, String sBody){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "CoolMic");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
             e.printStackTrace();
        }
    }

    private void setLoggingHandlers() {
        recordingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case VorbisRecorder.START_ENCODING:
                    	  logMessage("Connected to server!");
                        break;
                    case VorbisRecorder.STOP_ENCODING:
                   	 	start_button.clearAnimation();
                   	 	start_button.setBackground(buttonColor);
                   	 	start_button.setText("Start Broadcast");
                    	logMessage("Stopping the broadcasting");
                        break;
                    case VorbisRecorder.UNSUPPORTED_AUDIO_TRACK_RECORD_PARAMETERS:
                        logMessage("Your device does not support this configuration");
                        break;
                    case VorbisRecorder.ERROR_INITIALIZING:
                    	logMessage("Error in initialization.Try changin audio configuration");
                        break;
                    case VorbisRecorder.FAILED_FOR_UNKNOWN_REASON:
                   	 	start_button.clearAnimation();
                   	 	start_button.setBackground(buttonColor);
                   	 	start_button.setText("Start Broadcast");
                    	  logMessage("Failed for unknown reason !");
                        break;
                    case VorbisRecorder.FINISHED_SUCCESSFULLY:
                   	 	start_button.clearAnimation();
                   	 	start_button.setBackground(buttonColor);
                   	 	start_button.setText("Start Broadcast");
                    	logMessage("Broadcasting Stop Successfully");
                     break;
                }
            }
        };

        playbackHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case VorbisPlayer.PLAYING_FAILED:
                        logMessage("The decoder failed to playback the file, check logs for more details");
                        break;
                    case VorbisPlayer.PLAYING_FINISHED:
                        logMessage("The decoder finished successfully");
                        break;
                    case VorbisPlayer.PLAYING_STARTED:
                        logMessage("Starting to decode");
                        break;
                }
            }
        };
    }
    public Object loadSerializedObject(File f)
    {
        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Object o = ois.readObject();
            return o;
        }
        catch(Exception ex)
        {
        	Log.v("Serialization Read Error : ",ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    @Override
    public void onBackPressed() {
        // Write your code here
    	if(isThreadOn){
		    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
				alertDialog.setTitle("Do want to stop broadcasting ?");
				alertDialog.setMessage("Broadcasting will stop after leaving the screen ?");
				alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	backyes=false;
		            	 dialog.cancel();
		            	}
				}); 
				alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog,int which) {
		            	backyes=true;
		            	 dialog.cancel();
		              	invalidateOptionsMenu();
		           	 	 start_button.clearAnimation();   
		           	 	 start_button.setBackground(buttonColor);
		           	 	 start_button.setText("Start Broadcast");
		            	   if (vorbisRecorder != null && vorbisRecorder.isRecording()) {
		                       try{
		                    	   isThreadOn=false;
		                           vorbisRecorder.stop();
		                           s.close();
		                          }catch (IOException e) {
		                           e.printStackTrace();
		                           Log.e("VS", "IOException",e);
		                       }
		                   }
		            	   android.os.Process.killProcess(android.os.Process.myPid());
		            }
				}); 
				alertDialog.show();
    	}else{
    		android.os.Process.killProcess(android.os.Process.myPid());
    	}
    }
    public void startRecording(View view) {
        if(isOnline()){ 
        	if(coolmic.isConnectionSet()){ 
        		invalidateOptionsMenu();
            	isThreadOn=true;
        		if(Boolean.valueOf((coolmic.getTermCondition()))){
        			start_button.startAnimation(animation);
						start_button.setBackground(trans);
						trans.startTransition(5000); 
						start_button.setText("Broadcasting");
			        	 streamThread = new Thread(new Runnable() {
			      	        @Override
			      	        public void run() {
			      	        	if(isThreadOn){
			     					try {
				     						String portnum = "";
			     							String server=coolmic.getServerName();
			     							Integer port_num=8000;
			     							if(server.indexOf(":") > 0){
			     				        		String[] split = server.split(":");
			     				    			server = split[0];
			     				    			portnum = split[1];
			     				    			port_num=Integer.parseInt(portnum);
			     				        	}
			     							Log.d("VS",server);		
			     							Log.d("VS",port_num.toString());
			     					    	String username=coolmic.getUsername();
			     					    	String password=coolmic.getPassword();
			     					    	String auth=username+":"+password;
			     					    	auth = username+":"+password;
			     					    	String mountpoint=coolmic.getMountpoint();
			     					    	String sampleRate_string=coolmic.getSampleRate();
			     					    	String channel_string=coolmic.getChannels();
			     					    	String quality_string=coolmic.getQuality();
			     					    	String title=coolmic.getTitle();
			     					    	String generalUsername=coolmic.getGeneralUsername();
			     					    	Log.d("VS",server+" "+server+" "+port_num.toString()+" "+username+" "+password+"\n "+mountpoint+"" +
			     					    			" "+sampleRate_string+" "+channel_string+" "+quality_string+" "+title);
			     					    	byte[] data = null;
			     					    	try {
			     					    	    data = auth.getBytes("UTF-8");
			     					    	} catch (UnsupportedEncodingException e1) {
			     					    	    e1.printStackTrace();
			     					    	}
			     					    	String authString = Base64.encodeToString(data, Base64.NO_WRAP);   					    	
			 					    		s = new Socket(server,port_num);
											Log.d("VS", "Socket Created");
											out =  new BufferedOutputStream(new DataOutputStream(s.getOutputStream()));
											Log.d("VS", "Output Stream Established");
											output = new PrintWriter(out);
											Log.d("VS", "Send Header");
											output.println("SOURCE /"+mountpoint+" ICE/1.0");
												output.println("Authorization: Basic "+authString);
												output.println("ice-name:"+title);
												output.println("ice-title:"+title);
												output.println("ice-artist:"+generalUsername);
												output.println("ice-url:echonet.cc");
												output.println("ice-username:"+generalUsername); 
												output.println("ice-user:"+generalUsername);
												output.println("content-type: application/x-ogg");
												output.println("User-Agent: Cool Mic App");						
												output.println("ice-private: 0");
												output.println("ice-public: 1");
												output.println("ice-audio-info: ice-samplerate="+sampleRate_string+"ice-quality="+quality_string+";ice-channels="+channel_string);
												output.println("ice-audio-info: ice-samplerate=8000;ice-bitrate=128;ice-channels=2");
												output.println("\r\n");
												output.println("\n");
											output.flush();
											Log.d("VS", "Header sent"); 
											reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
											for (String line; (line = reader.readLine()) != null;) {
											    if (line.equals("")) break;
											    	Log.d("VS", "Responce From Server");
										    		Log.d("VS",line);
			 									 }
											 if (vorbisRecorder == null || vorbisRecorder.isStopped()) {						      
				     								if (vorbisRecorder == null) {
				     									Log.d("VS","Before recorder initilize");
				     									vorbisRecorder = new VorbisRecorder(out, recordingHandler);
				     								}
				     								long sampleRate = Long.parseLong(coolmic.getSampleRate());
				     								long channels = Long.parseLong(coolmic.getChannels());
				     								float quality=Float.parseFloat(coolmic.getQuality());
				     								Log.d("VS","Before start method");
				     								vorbisRecorder.start(sampleRate, channels,quality);
				     						   }
			     						 } catch(UnknownHostException e) {
			     						         Log.e("VS", "UnknownHostException",e);
			     						 } catch (IOException e) {
			     						    e.printStackTrace();
			     						    Log.e("VS", "IOException",e);
			     						 }catch(Exception e){
			     							  e.printStackTrace();
				     						    Log.e("VS", "IOException",e);
			     						 }
			      	        }
			      	        
			      	        }
			        	 
			        	 });
			        	   streamThread.start(); 
        		}else{
        			Toast.makeText(getApplicationContext(), "Accept the Term and Conditions !", Toast.LENGTH_LONG).show();
        		}
        	}else{
        		Toast.makeText(getApplicationContext(), "Set the connection details !", Toast.LENGTH_LONG).show();	        	
        	}
        }else{
        	Toast.makeText(getApplicationContext(), "Check Internet Connection !", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("deprecation")
	public void stopRecording(@SuppressWarnings("unused") View view) {
    	invalidateOptionsMenu(); 
   	 	 start_button.clearAnimation();   
   	 	 start_button.setBackground(buttonColor);
   	 	 start_button.setText("Start Broadcast");
    	   if (vorbisRecorder != null && vorbisRecorder.isRecording()) {
               try{
            	   isThreadOn=false;
                   vorbisRecorder.stop();
                   s.close();
                  }catch (IOException e) {
                   e.printStackTrace();
                   Log.e("VS", "IOException",e);
               }
           }
 			Intent i = new Intent(MainActivity.this, MainActivity.class);
 			startActivity(i);
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
}
