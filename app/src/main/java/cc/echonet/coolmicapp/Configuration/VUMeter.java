package cc.echonet.coolmicapp.Configuration;

import cc.echonet.coolmicapp.R;

public class VUMeter extends ProfileBase {
    VUMeter(ProfileBase profile) {
        super(profile);
    }

    public int getInterval() {
        return Integer.parseInt(getString("audio_interval", R.string.pref_default_vumeter_interval));
    }
}
