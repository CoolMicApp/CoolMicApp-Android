package cc.echonet.coolmicapp.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cc.echonet.coolmicapp.R;

abstract class ProfileBase {
    Context context;
    SharedPreferences prefs;

    ProfileBase(Context context, String profile) {
        this.context = context;
        this.prefs = context.getSharedPreferences(profile, Context.MODE_PRIVATE);

        if (!this.prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(context, profile, Context.MODE_PRIVATE, R.xml.pref_all, true);

            this.prefs.edit().putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        }
    }

    ProfileBase(ProfileBase profile) {
        this.context = profile.context;
        this.prefs = profile.prefs;
    }

    String getString(String key) {
        return getString(key, "");
    }

    String getString(String key, String def) {
        return prefs.getString(key, def);
    }

    String getString(String key, int def) {
        return getString(key, context.getString(def));
    }

    boolean getBoolean(String key, boolean def) {
        return prefs.getBoolean(key, def);
    }

}
