package com.vorbisdemo;


import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class audio extends Activity {
	  private Spinner sampleRateSpinner;
	  private Spinner chanelConfigSpinner;
	  private Spinner qualitySpinner;
	  //  CoolMicSetting newSetting;
	  DatabaseHandler db ;
	  CoolMic coolmic;
	  boolean flag=false;
	  int i=0;
	  int j=0;
	  int k=0;
	   
	  @Override
      public boolean onCreateOptionsMenu(Menu menu) {
		  MenuInflater inflater = getMenuInflater();
		  inflater.inflate(R.menu.main_activity_menu, menu);
		  menu.findItem(R.id.audio_settings).setVisible(false);
		  return true;
	  }
	  private void setDefaultValues() {
		  //Set sample rate to '44100'
		  String samplerate= coolmic.getSampleRate();
		  ArrayAdapter myAdap = (ArrayAdapter) sampleRateSpinner.getAdapter(); 
		  int spinnerPosition = myAdap.getPosition(samplerate);
		  sampleRateSpinner.setSelection(spinnerPosition);
		  //String chanelConfig= Long.toString(newSetting.getChannels()-1);
		  //	ArrayAdapter chanelConfigSpinnerAdap = (ArrayAdapter) chanelConfigSpinner.getAdapter(); 
		  //	int chanelConfigspinnerPosition = chanelConfigSpinnerAdap.getPosition(chanelConfig);
		  chanelConfigSpinner.setSelection(Integer.parseInt(coolmic.getChannels())-1);
		  String quality= coolmic.getQuality();
		  Log.d("VS","quality"+quality);
		  ArrayAdapter qualitySpinnerAdap = (ArrayAdapter) qualitySpinner.getAdapter();
		  int qualityspinnerPosition = qualitySpinnerAdap.getPosition(quality);
		  Log.d("VS","quality"+qualityspinnerPosition);
		  qualitySpinner.setSelection(qualityspinnerPosition);
	  }
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
    	  switch (item.getItemId()) {
    	  // action with ID action_refresh was selected
    	  case R.id.home:
    		  goHome();
    		  // onBackPressed();
    		  return true;
	      // action with ID action_settings was selected
	      case R.id.general_setting:
	    	  generalSetting();
	    	  return true;
	      case R.id.server_settings:
	    	  serverSetting();
	    	  return true;
	      case R.id.audio_settings:
	    	  //audioSetting();
	    	  return true;
	      case R.id.help:
	          Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://coolmic.net/help/"));
	          startActivity(helpIntent);
	    	  return true;	  
	      case R.id.quite_app:
	    	  exitApp();
	    	  return true;
	      default:
	    	  goHome();
	        break;
	      }
	      return true;
	  }
    private void exitApp(){
    	   finish();
           System.exit(0);
    }
	private void goHome() {
		if(flag){
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(audio.this);
			alertDialog.setTitle("Save settings ?");
			alertDialog.setMessage("Do you want to save settings ?");
			alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
			            dialog.cancel();
			    		Intent i = new Intent(audio.this, MainActivity.class);
			    		startActivity(i);
			    		finish();
	            	}
			});
			alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
		            	saveAudioNavigation();
		        		Intent i = new Intent(audio.this, MainActivity.class);
		        		startActivity(i);
		        		finish();
	            }
			});
			alertDialog.show();			
		}else{
				Intent i = new Intent(audio.this, MainActivity.class);
				startActivity(i);		
				finish();
		}
	}
	private void generalSetting() {
		
		if(flag){
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(audio.this);
			alertDialog.setTitle("Save settings ?");
			alertDialog.setMessage("Do you want to save settings ?");
			alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		    		Intent i = new Intent(audio.this, general.class);
		    		startActivity(i);
		    		finish();
	            	}
			});
			alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	            	saveAudioNavigation();
	        		Intent i = new Intent(audio.this, general.class);
	        		startActivity(i);
	        		finish();
	            }
			});
			alertDialog.show();			
		}else{
			Intent i = new Intent(audio.this, general.class);
			startActivity(i);	
			finish();
		}
		
	}
	private void serverSetting() {
		
		if(flag){
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(audio.this);
			alertDialog.setTitle("Save settings ?");
			alertDialog.setMessage("Do you want to save settings ?");
			alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		    		Intent i = new Intent(audio.this, server.class);
		    		startActivity(i);
		    		finish();
	            	}
			});
			alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	            	saveAudioNavigation();
	        		Intent i = new Intent(audio.this, server.class);
	        		startActivity(i);
	        		finish();
	            }
			});
			alertDialog.show();
		}else{
			Intent i = new Intent(audio.this, server.class);
			startActivity(i);
			finish();
		}
		
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(audio.this, MainActivity.class);
		startActivity(i);
		finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		db = new DatabaseHandler(this);
		coolmic=db.getCoolMicDetails(1);
		
		//	newSetting=CoolMicSetting.getInstance();
	    sampleRateSpinner = (Spinner) findViewById(R.id.sample_rate_spinner);
	    chanelConfigSpinner = (Spinner) findViewById(R.id.channel_config_spinner);
	    qualitySpinner = (Spinner) findViewById(R.id.quality_spinner);
	    setDefaultValues();
	    sampleRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	    @SuppressWarnings("unused")
				@Override
	    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {	    	    		 
	    	    	if(i > 0){
	    	    		flag=true;	    	    		
	    	    	}	    
	    	    	i++;
	    	    }
	    	    @Override
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    	Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_LONG).show();	  
	    	    }
	    });
	      
	      
	    chanelConfigSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    	    	@SuppressWarnings("unused")
    	    	@Override
    	    	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    	    		if(j > 0){
    	    			flag=true;
    	    		}	    
    	    		j++;
    	    	}
    	    	@Override
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    	Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_LONG).show();	  
	    	    }
    	});
	    qualitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	    @SuppressWarnings("unused")
				@Override
	    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	    	if(k > 0){
	    	    		flag=true;	    	    		
	    	    	}
	    	    	k++;
	    	    }
	    	    @Override
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    	Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_LONG).show();
	    	    }
    	});
	}
	
	 public void saveAudio(@SuppressWarnings("unused") View view) { 
		 		flag=false;
	            Long sampleRate = Long.parseLong(sampleRateSpinner.getSelectedItem().toString());
	            Long channels = (long)(chanelConfigSpinner.getSelectedItemPosition() + 1) ;	                   
	            Float quality = Float.parseFloat(qualitySpinner.getSelectedItem().toString());
	            coolmic.setSampleRate(Long.toString(sampleRate));
	            coolmic.setChannels(Long.toString(channels));
	            coolmic.setQuality(Float.toString(quality));
		  		if(db.updateCoolMicDetails(coolmic)==1){
		  			Toast.makeText(getApplicationContext(), "Audio settings saved!", Toast.LENGTH_LONG).show();
		  		}else{
		         	Toast.makeText(getApplicationContext(), "Error to save Audio Setting", Toast.LENGTH_LONG).show();
		  		}
	            /*
	            if(CoolMicSetting.saveSetting(newSetting)){
	            	Toast.makeText(getApplicationContext(),"Audio settings saved!", Toast.LENGTH_LONG).show();
	            }else{
	            	Toast.makeText(getApplicationContext(), "Error to save Audio Setting", Toast.LENGTH_LONG).show();
	            }*/
	 }
	 public void saveAudioNavigation() {   
         Long sampleRate = Long.parseLong(sampleRateSpinner.getSelectedItem().toString());
         Long channels = (long)(chanelConfigSpinner.getSelectedItemPosition() + 1) ;	                   
         Float quality = Float.parseFloat(qualitySpinner.getSelectedItem().toString());
         
         coolmic.setSampleRate(Long.toString(sampleRate));
         coolmic.setChannels(Long.toString(channels));
         coolmic.setQuality(Float.toString(quality));
         
 		 if(db.updateCoolMicDetails(coolmic)==1){
			  	Toast.makeText(getApplicationContext(), "Audio settings saved!", Toast.LENGTH_LONG).show();
		 }else{
	         	Toast.makeText(getApplicationContext(), "Error to save Audio Setting", Toast.LENGTH_LONG).show();
	     }
         /*
         if(CoolMicSetting.saveSetting(newSetting)){
         	Toast.makeText(getApplicationContext(),"Audio settings saved!", Toast.LENGTH_LONG).show();
         }else{
         	Toast.makeText(getApplicationContext(), "Error to save Audio Setting", Toast.LENGTH_LONG).show();
         }*/
	 }
}