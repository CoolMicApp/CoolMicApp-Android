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


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import androidx.core.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import cc.echonet.coolmicapp.BackgroundService.Client.Client;
import cc.echonet.coolmicapp.Configuration.Codec;
import cc.echonet.coolmicapp.Configuration.DialogIdentifier;
import cc.echonet.coolmicapp.Configuration.DialogState;
import cc.echonet.coolmicapp.Configuration.Manager;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Server;

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
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
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
            } else if (preference instanceof EditTextPreference) {
                EditTextPreference prefText = (EditTextPreference) preference;
                // For all other preferences, set the summary to the value's
                // simple string representation.
                prefText.setSummary(stringValue);
                prefText.setDefaultValue(stringValue);
                prefText.setText(stringValue);
            } else {
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
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, preference.getPreferenceManager().getSharedPreferences().getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);

        setupActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        Client client;

        super.onDestroy();

        client = new Client(this, null);
        client.reloadParameters();
        client.disconnect();
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!Utils.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
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

            getPreferenceManager().setSharedPreferencesName((new Manager(getActivity())).getGlobalConfiguration().getCurrentProfileName());
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

            //Hardcode some required values for Opus
            findPreference("audio_codec").setOnPreferenceChangeListener((preference, value) -> {
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
            });

            Preference button_util_conn_default = getPreferenceManager().findPreference("util_conn_default");
            if (button_util_conn_default != null) {
                button_util_conn_default.setOnPreferenceClickListener(arg0 -> {
                    AlertDialog.Builder alertDialog = Utils.buildAlertDialogCMTSTOS(getActivity());
                    alertDialog.setPositiveButton(R.string.mainactivity_missing_connection_details_yes, (dialog, which) -> {
                        Utils.loadCMTSData(getActivity().getApplicationContext(), (new Manager(getActivity())).getCurrentProfile());

                        refreshSummaryForConnectionSettings();
                        handleSampleRateEnabled();
                    });

                    alertDialog.show();


                    return true;
                });
            }

            Preference button_util_qr_scan = getPreferenceManager().findPreference("util_qr_scan");
            if (button_util_qr_scan != null) {
                button_util_qr_scan.setOnPreferenceClickListener(arg0 -> {

                    IntentIntegrator integrator = new IntentIntegrator(PrefsFragment.this);
                    integrator.setTitle("Please scan a fully qualified URI");
                    integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);

                    return true;
                });
            }

            Preference util_permission_check = getPreferenceManager().findPreference("util_permission_check");
            if (util_permission_check != null) {
                util_permission_check.setOnPreferenceClickListener(arg0 -> {
                    Utils.requestPermissions(getActivity());

                    return true;
                });
            }

            Preference util_reset_dialogs = getPreferenceManager().findPreference("util_reset_dialogs");
            if (util_reset_dialogs != null) {
                util_reset_dialogs.setOnPreferenceClickListener(preference -> {
                    Profile profile = (new Manager(getActivity())).getCurrentProfile();
                    for (DialogIdentifier dialogIdentifier : DialogIdentifier.values()) {
                        profile.getDialogState(dialogIdentifier).reset();
                    }
                    Toast.makeText(getActivity().getApplicationContext(), R.string.pref_title_utility_reset_dialogs_done, Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            handleSampleRateEnabled();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent intent) {

            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Uri u;
                Profile profile;
                Server server;
                String mountpoint;

                try {
                    u = Uri.parse(scanResult.getContents());
                } catch (Exception e1) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.settingsactivity_url_invalid, Toast.LENGTH_LONG).show();
                    return;
                }

                profile = (new Manager(getActivity())).getCurrentProfile();
                profile.edit();
                server = profile.getServer();

                if (u.getUserInfo() != null && u.getUserInfo().split(":").length >= 2) {
                    String[] authority = u.getUserInfo().split(":");

                    server.setUsername(authority[0]);
                    server.setPassword(authority[1]);
                } else {
                    final Uri uf = u;
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(R.string.settings_qrcode_invalid_noauth_title);
                    alertDialog.setMessage(getString(R.string.settings_qrcode_invalid_noauth_message, u.toString()));

                    alertDialog.setNegativeButton(R.string.mainactivity_quit_cancel, (dialog, which) -> dialog.cancel());

                    alertDialog.setPositiveButton(R.string.mainactivity_quit_ok, (dialog, which) -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(uf);
                        startActivity(i);
                    });

                    alertDialog.show();

                    return;
                }

                server.setAddress(u.getHost(), u.getPort());

                try {
                    mountpoint = u.getPath().replaceAll("^/", "");

                    if (mountpoint.endsWith(".opus")) {
                        profile.getCodec().setType(Codec.TYPE_OPUS);
                    }
                    server.setMountpoint(mountpoint);
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.settingsactivity_cannot_get_mountpoint, Toast.LENGTH_SHORT).show();
                }

                profile.apply();

                handleSampleRateEnabled();

                refreshSummaryForConnectionSettings();

                Toast.makeText(getActivity().getApplicationContext(), R.string.settingsactivity_qrcode_loaded, Toast.LENGTH_LONG).show();
            }
        }

        private void handleSampleRateEnabled() {

            String codec = getPreferenceManager().getSharedPreferences().getString("audio_codec", getString(R.string.pref_default_audio_codec));


            handleSampleRateEnabled(!codec.equals("audio/ogg; codec=opus"));
        }

        private void handleSampleRateEnabled(boolean enabled) {
            findPreference("audio_samplerate").setEnabled(enabled);
        }

        private void refreshSummaryForConnectionSettings() {
            List<Preference> preferencesToUpdate = new ArrayList<>();
            preferencesToUpdate.add(findPreference("connection_address"));
            preferencesToUpdate.add(findPreference("connection_username"));
            preferencesToUpdate.add(findPreference("connection_mountpoint"));
            preferencesToUpdate.add(findPreference("audio_codec"));
            preferencesToUpdate.add(findPreference("audio_samplerate"));

            EditTextPreference passwordPref = (EditTextPreference) findPreference("connection_password");
            String passwordValue = getPreferenceManager().getSharedPreferences().getString(passwordPref.getKey(), "");

            passwordPref.setDefaultValue(passwordValue);
            passwordPref.setText(passwordValue);

            for (Preference preference : preferencesToUpdate) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, getPreferenceManager().getSharedPreferences().getString(preference.getKey(), ""));
            }
        }
    }
}
