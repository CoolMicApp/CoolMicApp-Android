package cc.echonet.coolmicapp.Configuration;

public class Track extends ProfileBase {
    Track(ProfileBase profile) {
        super(profile);
    }

    public String getArtist() {
        return getString("general_artist");
    }

    public String getTitle() {
        return getString("general_title");
    }

}
