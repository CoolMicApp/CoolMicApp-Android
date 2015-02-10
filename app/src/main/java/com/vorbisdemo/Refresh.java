package com.vorbisdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Refresh extends Activity {

    DatabaseHandler db;
    CoolMic coolmic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh);
        db = new DatabaseHandler(this);

        coolmic = db.getCoolMicDetails(1);
        String sr = coolmic.getSampleRate();
        coolmic.setSampleRate("8000");
        db.updateCoolMicDetails(coolmic);

        coolmic = db.getCoolMicDetails(1);
        coolmic.setSampleRate("11025");
        db.updateCoolMicDetails(coolmic);

        coolmic = db.getCoolMicDetails(1);
        coolmic.setSampleRate(sr);
        ;
        db.updateCoolMicDetails(coolmic);

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Broadcast stopped succesfully !", Toast.LENGTH_LONG).show();
    }
}
