package com.vorbisdemo;

public class CoolMic {
    int id;
    private String artist = "";
    private String title = "";
    private String generalUsername = "";
    private String servername = "";
    private String mountpoint = "";
    private String username = "";
    private String password = "";
    private String sampleRate = "";
    private String channels = "";
    private String quality = "";
    private String termCondition = "";

    /*   public CoolMic(int id, String title){
            this.id = id;
            this.title = title;
       }
       public CoolMic(int id,String title,String generalUsername){
               this.id = id;
           this.title = title;
           this.generalUsername = generalUsername;
      }
       public CoolMic(String title,String generalUsername){
           this.title = title;
           this.generalUsername = generalUsername;
      }*/
    // constructor
    public CoolMic(int id, String title, String artist, String generalUsername, String servername, String mountpoint,
                   String username, String password, String sampleRate, String channels, String quality, String termCondition) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.generalUsername = generalUsername;
        this.servername = servername;
        this.mountpoint = mountpoint;
        this.username = username;
        this.password = password;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.quality = quality;
        this.termCondition = termCondition;
    }

    // constructor
    public CoolMic(String title, String artist, String generalUsername, String servername, String mountpoint,
                   String username, String password, String sampleRate, String channels, String quality, String termCondition) {
        this.title = title;
        this.artist = artist;
        this.generalUsername = generalUsername;
        this.servername = servername;
        this.mountpoint = mountpoint;
        this.username = username;
        this.password = password;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.quality = quality;
        this.termCondition = termCondition;
    }

    // getting ID
    public int getID() {
        return this.id;
    }

    // setting id
    public void setID(int id) {
        this.id = id;
    }

    public String getTermCondition() {
        return termCondition;
    }

    public void setTermCondition(String termCondition) {
        this.termCondition = termCondition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGeneralUsername() {
        return generalUsername;
    }

    public void setGeneralUsername(String generalUsername) {
        this.generalUsername = generalUsername;
    }

    public String getServerName() {
        return servername;
    }

    public void setServerName(String servername) {
        this.servername = servername;
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public boolean isConnectionSet() {
        if ((this.servername != null && !this.servername.isEmpty()) && (this.mountpoint != null && !this.mountpoint.isEmpty()) &&
                (this.username != null && !this.username.isEmpty()) && ((this.password != null && !this.password.isEmpty()))) {
            return true;
        } else {
            return false;
        }
    }
}
