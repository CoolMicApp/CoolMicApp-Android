package cc.echonet.coolmicapp.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GlobalConfiguration {
    private static final String PROFILE_NAME = "_global";
    private static final String KEY_PROFILE_CURRENT = "profile_current";

    private Context context;
    private SharedPreferences prefs;

    public GlobalConfiguration(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PROFILE_NAME, Context.MODE_PRIVATE);

        setDefaults();
    }

    private void setDefaults() {
        SharedPreferences.Editor editor;

        if (prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false))
            return;

        editor = prefs.edit();

        editor.putString(KEY_PROFILE_CURRENT, Manager.DEFAULT_PROFILE);

        editor.putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        editor.apply();
    }

    public String getCurrentProfileName() {
        return prefs.getString(KEY_PROFILE_CURRENT, Manager.DEFAULT_PROFILE);
    }

    public void setCurrentProfileName(String profileName) {
        Profile.assertValidProfileName(profileName);
        prefs.edit().putString(KEY_PROFILE_CURRENT, profileName).apply();
    }
}
