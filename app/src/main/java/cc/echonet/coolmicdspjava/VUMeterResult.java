package cc.echonet.coolmicdspjava;

/**
 * Created by stephanj on 23.05.2016.
 */
public class VUMeterResult {

    public VUMeterResult() {

    }

    public int rate;
    public int channels;

    public long frames;

    public int global_peak;
    public double global_power;

    public int[] channels_peak;
    public double[] channels_power;
}
