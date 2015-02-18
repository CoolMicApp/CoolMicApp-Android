package com.vorbisdemo;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
	
	private static final int LED_NOTIFICATION_ID = 0;
	private boolean ThreadStatus=false;
	private NotificationManager NotificationManager;
    public void setThreadStatus(boolean status){
    	this.ThreadStatus=status;
    }
    public void setNT(NotificationManager nf)
    {
    	this.NotificationManager=nf;
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {	 
       if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                Log.v("$$$$$$", "In Method:  ACTION_SCREEN_OFF");
                //     Toast.makeText(context, "Intent ACTION_SCREEN_OFF.", Toast.LENGTH_LONG).show();
                // onPause() will be called.
       }
       else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){  
        	 	// Toast.makeText(context, "Intent ACTION_SCREEN_ON.", Toast.LENGTH_LONG).show();
              	Log.v("$$$$$$", "In Method:  ACTION_SCREEN_ON"); 
              	//onResume() will be called.
              	//Better check for whether the screen was already locked
              	//if locked, do not take any resuming action in onResume()
                //Suggest you, not to take any resuming action here.       
       }
       else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) 
       {  
        	   	//  Toast.makeText(context, "Intent ACTION_USER_PRESENT.", Toast.LENGTH_LONG).show();
              	Log.v("$$$$$$", "In Method:  ACTION_USER_PRESENT");
              	//Handle resuming events
       }
      
    }
 
}
    