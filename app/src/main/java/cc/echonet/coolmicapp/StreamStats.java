package cc.echonet.coolmicapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by stephanj on 21.03.2016.
 */
public class StreamStats implements Parcelable {
    private int listeners_current;
    private int listeners_peak;

    StreamStats(int listeners_current, int listeners_peak) {
        this.listeners_current = listeners_current;
        this.listeners_peak = listeners_peak;
    }

    public int getListenersCurrent() {
        return listeners_current;
    }

    public void setListenersCurrent(int listeners_current) {
        this.listeners_current = listeners_current;
    }

    public int getListenersPeak() {
        return listeners_peak;
    }

    public void setListenersPeak(int listeners_peak) {
        this.listeners_peak = listeners_peak;
    }

    //--------------------------------  METHODS FOR PARCELABLE -----------------------------------

    public static final Creator CREATOR = new Creator() {
        public StreamStats createFromParcel(Parcel in) {
            return new StreamStats(in);
        }

        public StreamStats[] newArray(int size) {
            return new StreamStats[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(listeners_current);
        dest.writeInt(listeners_peak);
    }

    public StreamStats(){

    }

    /**
     * This will be used only by the MyCreator
     *
     * @param source source where to read the parceled data
     */
    public StreamStats(Parcel source) {
        // reconstruct from the parcel
        listeners_current = source.readInt();
        listeners_peak = source.readInt();
    }
}
