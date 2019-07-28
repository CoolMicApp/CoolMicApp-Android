package cc.echonet.coolmicapp.Configuration;

import cc.echonet.coolmicapp.R;

public class Audio extends ProfileBase {
    Audio(ProfileBase profile) {
        super(profile);
    }

    public int getSampleRate() {
        return Integer.parseInt(getString("audio_samplerate", R.string.pref_default_audio_samplerate));
    }

    public void setSampleRate(int sampleRate) {
        editor.putString("audio_samplerate", Integer.toString(sampleRate));
    }

    public int getChannels() {
        return Integer.parseInt(getString("audio_channels", R.string.pref_default_audio_channels));
    }
}
