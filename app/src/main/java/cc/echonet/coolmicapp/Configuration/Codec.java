package cc.echonet.coolmicapp.Configuration;

import cc.echonet.coolmicapp.R;

public class Codec extends ProfileBase {
    private Audio audio;

    Codec(ProfileBase profile, Audio audio) {
        super(profile);
        this.audio = audio;
    }

    public Audio getAudio() {
        return audio;
    }

    public String getType() {
        return getString("audio_codec", R.string.pref_default_audio_codec);
    }

    public double getQuality() {
        return Double.parseDouble(getString("audio_quality", R.string.pref_default_audio_quality));
    }
}
