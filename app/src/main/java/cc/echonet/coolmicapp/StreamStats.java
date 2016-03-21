package cc.echonet.coolmicapp;

import java.io.Serializable;

/**
 * Created by stephanj on 21.03.2016.
 */
public class StreamStats implements Serializable {
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
}
