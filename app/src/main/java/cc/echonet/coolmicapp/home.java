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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class home extends Activity {


//	CoolMicSetting newSetting; 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        //   newSetting=CoolMicSetting.getInstance();
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
            case R.id.help:
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cc.echonet.coolmicapp.net/help/"));
                startActivity(helpIntent);
                return true;
            case R.id.quite_app:
                exitApp();
                return true;
            default:
                break;
        }

        return true;
    }

    private void exitApp() {
        finish();
        System.exit(0);
    }

    private void goHome() {
        Intent i = new Intent(home.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void generalSetting() {
        Intent i = new Intent(home.this, general.class);
        startActivity(i);
        finish();
    }

    private void audioSetting() {
        Intent i = new Intent(home.this, audio.class);
        startActivity(i);
        finish();
    }

    private void serverSetting() {
        Intent i = new Intent(home.this, server.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.home);
        } else {
            setContentView(R.layout.home_landscape);
        }
        // get the action bar
        ActionBar actionBar = getActionBar();
        // Enabling Back navigation on Action Bar icon
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
