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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class server extends Activity {

    //	 CoolMicSetting newSetting;
    DatabaseHandler db;
    CoolMic coolmic;
    boolean flag = false;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        flag = false;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.server_settings).setVisible(false);
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
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(server.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(server.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveConnectionNavigation();
                    Intent i = new Intent(server.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(server.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void generalSetting() {
        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(server.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(server.this, general.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveConnectionNavigation();
                    Intent i = new Intent(server.this, general.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(server.this, general.class);
            startActivity(i);
            finish();
        }
    }

    private void audioSetting() {
        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(server.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(server.this, audio.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveConnectionNavigation();
                    Intent i = new Intent(server.this, audio.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(server.this, audio.class);
            startActivity(i);
            finish();
        }
    }

    private void serverSetting() {
        if (flag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(server.this);
            alertDialog.setTitle("Save settings ?");
            alertDialog.setMessage("Do you want to save settings ?");
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(server.this, server.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveConnectionNavigation();
                    Intent i = new Intent(server.this, server.class);
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.show();
        } else {
            Intent i = new Intent(server.this, server.class);
            startActivity(i);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(server.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_setting);
        db = new DatabaseHandler(this);
        coolmic = db.getCoolMicDetails(1);
        flag = false;
        // get the action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        // newSetting=CoolMicSetting.getInstance();
        EditText server_edittext = (EditText) findViewById(R.id.server_edittext);
        EditText mountpoint_edittext = (EditText) findViewById(R.id.mountpoint_edittext);
        EditText username_editid = (EditText) findViewById(R.id.username_editid);
        final EditText password_edittext = (EditText) findViewById(R.id.password_edittext);
        CheckBox termCondition = (CheckBox) findViewById(R.id.term_condition_checkbx);
        mountpoint_edittext.setText("asd");
        server_edittext.addTextChangedListener(new TextWatcher() {
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

        mountpoint_edittext.addTextChangedListener(new TextWatcher() {
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
        username_editid.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                flag = true;
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                flag = true;
                EditText username_editid = (EditText) findViewById(R.id.username_editid);
                EditText mountpoint_edittext = (EditText) findViewById(R.id.mountpoint_edittext);
                mountpoint_edittext.setText("/" + username_editid.getText().toString() + ".ogg");
            }
        });
        password_edittext.addTextChangedListener(new TextWatcher() {
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

        CheckBox c = (CheckBox) findViewById(R.id.show_password_check);
        c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (!isChecked) {
                    password_edittext.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    password_edittext.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        try {
            server_edittext.setText(coolmic.getServerName());
            username_editid.setText(coolmic.getUsername());
            password_edittext.setText(coolmic.getPassword());
            mountpoint_edittext.setText("/" + coolmic.getMountpoint());
        } catch (Exception e) {
            Log.v("VS", e.getMessage());
        }

        //	 DatabaseHandler db = new DatabaseHandler(this);
        //	 CoolMic cm=db.getContact(1);
        //       String log = " her Id: "+cm.getID()+" , her title: " + cm.getTitle() + " ,generalUsername: " + cm.getGeneralUsername()+", servername: "+cm.getServerName()+" , mountpoint: "+cm.getMountpoint()+", username: "+cm.getUsername()+", password: "+cm.getPassword()+", sampleRate: "+cm.getSampleRate()+", channels: "+cm.getChannels()	+", quality: "+cm.getQuality()+", termCondition: "+cm.getTermCondition();
        //    Log.d("VS", log);
    }

    public void termConditionLink(@SuppressWarnings("unused") View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://echonet.cc/terms/cc.echonet.coolmicapp/"));
        startActivity(browserIntent);
    }

    public void saveConnection(@SuppressWarnings("unused") View view) {
        flag = false;
        EditText server_edittext = (EditText) findViewById(R.id.server_edittext);
        EditText mountpoint_edittext = (EditText) findViewById(R.id.mountpoint_edittext);
        EditText username_editid = (EditText) findViewById(R.id.username_editid);
        EditText password_edittext = (EditText) findViewById(R.id.password_edittext);

        String servername = server_edittext.getText().toString();
        String mountpoint = (mountpoint_edittext.getText().toString()).replaceAll("[/]", "");
        String username = username_editid.getText().toString();
        String password = password_edittext.getText().toString();

        coolmic.setServerName(servername);
        coolmic.setMountpoint(mountpoint);
        coolmic.setUsername(username);
        coolmic.setGeneralUsername(username);
        coolmic.setPassword(password);
        if (db.updateCoolMicDetails(coolmic) == 1) {
            Toast.makeText(getApplicationContext(), "Connection settings saved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Error to save Connection Setting", Toast.LENGTH_LONG).show();
        }

		 /*  if(CoolMicSetting.saveSetting(newSetting)){
             Toast.makeText(getApplicationContext(), "Connection settings saved!", Toast.LENGTH_LONG).show();
         }else{
         	Toast.makeText(getApplicationContext(), "Error to save Connection Setting", Toast.LENGTH_LONG).show();
         }*/
    }

    public void saveConnectionNavigation() {
        EditText server_edittext = (EditText) findViewById(R.id.server_edittext);
        EditText mountpoint_edittext = (EditText) findViewById(R.id.mountpoint_edittext);
        EditText username_editid = (EditText) findViewById(R.id.username_editid);
        EditText password_edittext = (EditText) findViewById(R.id.password_edittext);

        String servername = server_edittext.getText().toString();
        String mountpoint = (mountpoint_edittext.getText().toString()).replaceAll("[/]", "");
        String username = username_editid.getText().toString();
        String password = password_edittext.getText().toString();

        coolmic.setServerName(servername);
        coolmic.setMountpoint(mountpoint);
        coolmic.setUsername(username);
        coolmic.setGeneralUsername(username);
        coolmic.setPassword(password);
        if (db.updateCoolMicDetails(coolmic) == 1) {
            Toast.makeText(getApplicationContext(), "Connection settings saved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Error to save Connection Setting", Toast.LENGTH_LONG).show();
        }
		 /*          
         if(CoolMicSetting.saveSetting(newSetting)){
         	Toast.makeText(getApplicationContext(), "Connection settings saved!", Toast.LENGTH_LONG).show();
         }else{
         	Toast.makeText(getApplicationContext(), "Error to save Connection Setting", Toast.LENGTH_LONG).show();
         }*/
    }
}
