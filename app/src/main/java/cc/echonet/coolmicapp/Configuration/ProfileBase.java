package cc.echonet.coolmicapp.Configuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cc.echonet.coolmicapp.R;

abstract class ProfileBase {
    Context context;
    String profileName;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public static void assertValidProfileName(String profileName) {
        if (profileName.startsWith("_"))
            throw new IllegalArgumentException("Bad Profile name: "+profileName);
    }

    ProfileBase(Context context, String profileName) {
        assertValidProfileName(profileName);

        this.context = context;
        this.profileName = profileName;
        this.prefs = context.getSharedPreferences(profileName, Context.MODE_PRIVATE);

        if (!this.prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(context, profileName, Context.MODE_PRIVATE, R.xml.pref_all, true);

            this.prefs.edit().putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        }
    }

    ProfileBase(ProfileBase profile) {
        this.context = profile.context;
        this.prefs = profile.prefs;
        this.editor = profile.editor;
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

    public String getName() {
        return profileName;
    }

    public Context getContext() {
        return context;
    }

    @SuppressLint("CommitPrefEdits")
    public void edit() {
        editor = prefs.edit();
    }

    public void apply() {
        editor.apply();
    }
}
