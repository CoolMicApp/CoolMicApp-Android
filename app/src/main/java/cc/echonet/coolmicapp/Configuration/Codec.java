package cc.echonet.coolmicapp.Configuration;

import android.content.SharedPreferences;

import cc.echonet.coolmicapp.R;

public class Codec extends ProfileBase {
    public static final String TYPE_OPUS = "audio/ogg; codec=opus";

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

    public void setType(String type) {
        editor.putString("audio_codec", type);

        if (type.equals(TYPE_OPUS)) {
            /* Opus only supports 48kHz */
            audio.setSampleRate(48000);
        }
    }

    public double getQuality() {
        return Double.parseDouble(getString("audio_quality", R.string.pref_default_audio_quality));
    }
}
