package cc.echonet.coolmicapp.Configuration;

public class Volume extends ProfileBase {
    private static final int DEFAULT_VOLUME = 100;

    private Audio audio;

    Volume(ProfileBase profile, Audio audio) {
        super(profile);

        this.audio = audio;
    }

    public Audio getAudio() {
        return audio;
    }

    public int getLeft() {
        return prefs.getInt("volume_left", DEFAULT_VOLUME);
    }

    public int getRight() {
        return prefs.getInt("volume_left", DEFAULT_VOLUME);
    }

    public void setLeft(int volume) {
        prefs.edit().putInt("volume_left", volume).apply();
    }

    public void setRight(int volume) {
        prefs.edit().putInt("volume_right", volume).apply();
    }
}
