package cc.echonet.coolmicapp;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                preference.setDefaultValue(index >= 0 ? listPreference.getEntries()[index] : null);
                ((ListPreference) preference).setValue(stringValue);
            } else if(preference instanceof  EditTextPreference) {
                EditTextPreference prefText = (EditTextPreference) preference;
                // For all other preferences, set the summary to the value's
                // simple string representation.
                prefText.setSummary(stringValue);
                prefText.setDefaultValue(stringValue);
                prefText.setText(stringValue);
            }
            else
            {
                preference.setSummary(stringValue);
                preference.setDefaultValue(stringValue);
            }

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, preference.getContext().getSharedPreferences("default", Context.MODE_PRIVATE).getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_CHECK_REQUEST_CODE:
                boolean permissions_ok = true;

                for(int i = 0; i < grantResults.length; i++)
                {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        permissions_ok = false;
                        Toast.makeText(this, getString(R.string.settingsactivity_permission_not_granted, permissions[i]), Toast.LENGTH_LONG).show();
                    }
                }

                if(permissions_ok)
                {
                    Toast.makeText(this, R.string.settingsactivity_permissions_all_granted, Toast.LENGTH_LONG).show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName) || PrefsFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName("default");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.pref_all);

            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("general_artist"));
            bindPreferenceSummaryToValue(findPreference("general_title"));

            bindPreferenceSummaryToValue(findPreference("connection_address"));
            bindPreferenceSummaryToValue(findPreference("connection_username"));
            bindPreferenceSummaryToValue(findPreference("connection_mountpoint"));


            bindPreferenceSummaryToValue(findPreference("audio_codec"));
            bindPreferenceSummaryToValue(findPreference("audio_channels"));
            bindPreferenceSummaryToValue(findPreference("audio_samplerate"));
            bindPreferenceSummaryToValue(findPreference("audio_quality"));

            bindPreferenceSummaryToValue(findPreference("vumeter_interval"));

            getPreferenceManager().setSharedPreferencesName("default");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            //Hardcode some required values for Opus
            findPreference("audio_codec").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String stringValue = value.toString();
                    SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();

                    String mountpoint = getPreferenceManager().getSharedPreferences().getString("connection_mountpoint", getString(R.string.pref_default_connection_mountpoint));

                    //Opus Codec name is supposed to be static
                    if (stringValue.equals("audio/ogg; codec=opus")) {
                        handleSampleRateEnabled(false);
                        mountpoint = mountpoint.replace("ogg", "opus");
                        editor.putString("audio_samplerate", getString(R.string.pref_default_audio_samplerate));
                    } else {
                        handleSampleRateEnabled(true);
                        mountpoint = mountpoint.replace("opus", "ogg");
                    }

                    editor.putString("connection_mountpoint", mountpoint);

                    editor.apply();

                    refreshSummaryForConnectionSettings();

                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, stringValue);

                    return true;
                }
            });

            Preference button_util_conn_default = getPreferenceManager().findPreference("util_conn_default");
            if (button_util_conn_default != null) {
                button_util_conn_default.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {

                        SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();

                        editor.putString("connection_address", getString(R.string.pref_default_connection_address));
                        editor.putString("connection_username", getString(R.string.pref_default_connection_username));
                        editor.putString("connection_password", getString(R.string.pref_default_connection_password));
                        editor.putString("connection_mountpoint", getString(R.string.pref_default_connection_mountpoint));
                        editor.putString("audio_codec", getString(R.string.pref_default_audio_codec));
                        editor.putString("audio_samplerate", getString(R.string.pref_default_audio_samplerate));

                        editor.apply();

                        refreshSummaryForConnectionSettings();

                        handleSampleRateEnabled();

                        Toast.makeText(getActivity(), R.string.settings_conn_defaults_loaded, Toast.LENGTH_LONG).show();

                        return true;
                    }
                });
            }

            Preference button_util_qr_scan = getPreferenceManager().findPreference("util_qr_scan");
            if (button_util_qr_scan != null) {
                button_util_qr_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {

                        IntentIntegrator integrator = new IntentIntegrator(PrefsFragment.this);
                        integrator.setTitle("Please scan a fully qualified URI");
                        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);

                        return true;
                    }
                });
            }

            Preference util_permission_check = getPreferenceManager().findPreference("util_permission_check");
            if (util_permission_check != null) {
                util_permission_check.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        if(!(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED))
                        {
                            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.INTERNET) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.ACCESS_NETWORK_STATE) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.READ_PHONE_STATE))) {
                                Toast.makeText(getActivity(), R.string.settingsactivity_toast_permission_denied, Toast.LENGTH_SHORT).show();
                            } else {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE}, Constants.PERMISSION_CHECK_REQUEST_CODE);
                            }

                        }
                        else
                        {
                            Toast.makeText(getActivity(), R.string.settingsactivity_toast_permissions_granted, Toast.LENGTH_LONG).show();
                        }

                        return true;
                    }
                });
            }

            handleSampleRateEnabled();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent intent) {

            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Uri u;
                try {
                    u = Uri.parse(scanResult.getContents());
                } catch (Exception e1) {
                    Toast.makeText(getActivity(), R.string.settingsactivity_url_invalid, Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = getActivity().getSharedPreferences("default", Context.MODE_PRIVATE).edit();

                if(u.getUserInfo() != null && u.getUserInfo().split(":").length >= 2) {
                    String authority[] = u.getUserInfo().split(":");

                    editor.putString("connection_username", authority[0]);
                    editor.putString("connection_password", authority[1]);
                }
                else
                {
                    final Uri uf = u;
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(R.string.settings_qrcode_invalid_noauth_title);
                    alertDialog.setMessage(getString(R.string.settings_qrcode_invalid_noauth_message, u.toString()));

                    alertDialog.setNegativeButton(R.string.mainactivity_quit_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.setPositiveButton(R.string.mainactivity_quit_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(uf);
                            startActivity(i);
                        }
                    });

                    alertDialog.show();

                    return;
                }

                String host = u.getHost();

                if(u.getPort() != 8000)
                {
                    host = host+":"+u.getPort();
                }

                String mountpoint = u.getPath().replaceAll("^/", "");

                if(mountpoint.endsWith(".opus"))
                {
                    editor.putString("audio_codec", getString(R.string.pref_default_audio_codec));
                    editor.putString("audio_samplerate", getString(R.string.pref_default_audio_samplerate));

                }

                editor.putString("connection_address", host);



                editor.putString("connection_mountpoint", mountpoint);

                editor.apply();

                handleSampleRateEnabled();

                refreshSummaryForConnectionSettings();

                Toast.makeText(getActivity(), R.string.settingsactivity_qrcode_loaded, Toast.LENGTH_LONG).show();
            }
        }

        private void handleSampleRateEnabled() {

            String codec = getPreferenceManager().getSharedPreferences().getString("audio_codec", getString(R.string.pref_default_audio_codec));


            handleSampleRateEnabled(!codec.equals("audio/ogg; codec=opus"));
        }

        private void handleSampleRateEnabled(boolean enabled) {
            findPreference("audio_samplerate").setEnabled(enabled);
        }

        private void refreshSummaryForConnectionSettings()
        {
            List<Preference> preferencesToUpdate = new ArrayList<>();
            preferencesToUpdate.add(findPreference("connection_address"));
            preferencesToUpdate.add(findPreference("connection_username"));
            preferencesToUpdate.add(findPreference("connection_mountpoint"));
            preferencesToUpdate.add(findPreference("audio_codec"));
            preferencesToUpdate.add(findPreference("audio_samplerate"));

            EditTextPreference passwordPref = (EditTextPreference) findPreference("connection_password");
            String passwordValue = passwordPref.getContext().getSharedPreferences("default", Context.MODE_PRIVATE).getString(passwordPref.getKey(), "");

            passwordPref.setDefaultValue(passwordValue);
            passwordPref.setText(passwordValue);

            for(Preference preference: preferencesToUpdate) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, preference.getContext().getSharedPreferences("default", Context.MODE_PRIVATE).getString(preference.getKey(), ""));
            }
        }
    }
}
