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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class general extends Activity {

    //	CoolMicSetting newSetting;
    DatabaseHandler db;
    CoolMic coolmic;
    boolean flag = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        flag = false;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.general_setting).setVisible(false);
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
                goHome();
                break;
        }

        return true;
    }

    private void exitApp() {
        finish();
        System.exit(0);
    }

    private void goHome() {

        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(general.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(general.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveGeneralNavigation();
                    Intent i = new Intent(general.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(general.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }

    private void generalSetting() {

        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(general.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(general.this, general.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveGeneralNavigation();
                    Intent i = new Intent(general.this, general.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(general.this, general.class);
            startActivity(i);
            finish();
        }
    }

    private void audioSetting() {


        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(general.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(general.this, audio.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveGeneralNavigation();
                    Intent i = new Intent(general.this, audio.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(general.this, audio.class);
            startActivity(i);
            finish();
        }

    }

    private void serverSetting() {

        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(general.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(general.this, server.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveGeneralNavigation();
                    Intent i = new Intent(general.this, server.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(general.this, server.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(general.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        db = new DatabaseHandler(this);
        coolmic = db.getCoolMicDetails(1);
        // get the action bar
        ActionBar actionBar = getActionBar();

        // Enabling Back navigation on Action Bar icon
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // newSetting=CoolMicSetting.getInstance();
        EditText title_edittext = (EditText) findViewById(R.id.title_edittext);
        EditText artist_edittext = (EditText) findViewById(R.id.artist_edittext);

        title_edittext.setText(coolmic.getTitle());
        artist_edittext.setText(coolmic.getArtist());

        title_edittext.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                flag = true;
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                flag = true;
            }
        });

        artist_edittext.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                flag = true;
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                flag = true;
            }
        });
    }

    public void saveGeneral(@SuppressWarnings("unused") View view) {
        flag = false;
        EditText title_edittext = (EditText) findViewById(R.id.title_edittext);
        EditText artist_edittext = (EditText) findViewById(R.id.artist_edittext);
        String title = title_edittext.getText().toString();
        String artist = artist_edittext.getText().toString();

        coolmic.setArtist(artist);
        coolmic.setTitle(title);

        if (db.updateCoolMicDetails(coolmic) == 1) {
            Toast.makeText(getApplicationContext(), "General settings saved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Error to save General Setting", Toast.LENGTH_LONG).show();
        }
         /*
         if(CoolMicSetting.saveSetting(newSetting)){
         	Toast.makeText(getApplicationContext(), "General settings saved!", Toast.LENGTH_LONG).show();
         }else{
         	Toast.makeText(getApplicationContext(), "Error to save General Setting", Toast.LENGTH_LONG).show();
         }*/
    }

    public void saveGeneralNavigation() {
        EditText title_edittext = (EditText) findViewById(R.id.title_edittext);
        EditText artist_edittext = (EditText) findViewById(R.id.artist_edittext);
        String title = title_edittext.getText().toString();
        String artist = artist_edittext.getText().toString();

        coolmic.setArtist(artist);
        coolmic.setTitle(title);
        if (db.updateCoolMicDetails(coolmic) == 1) {
            Toast.makeText(getApplicationContext(), "General settings saved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Error to save General Setting", Toast.LENGTH_LONG).show();
        }
         /*  if(CoolMicSetting.saveSetting(newSetting)){
             Toast.makeText(getApplicationContext(), "General settings saved!", Toast.LENGTH_LONG).show();
         }else{
         	Toast.makeText(getApplicationContext(), "Error to save General Setting", Toast.LENGTH_LONG).show();
         }*/
    }
}