package cc.echonet.coolmicapp.Icecast.Response;

public class Stats extends Response {
    private int listenerCurrent;
    private int listenerPeak;

    public Stats(int listenerCurrent, int listenerPeak) {
        this.listenerCurrent = listenerCurrent;
        this.listenerPeak = listenerPeak;
    }

    public int getListenerCurrent() {
        return listenerCurrent;
    }

    public int getListenerPeak() {
        return listenerPeak;
    }
}
