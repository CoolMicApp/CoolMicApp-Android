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

    public void setChannelPeakPower(int channel, int peak, double power)
    {
        if(this.channels_peak == null)
        {
            this.channels_peak = new int[16];
        }

        if(this.channels_power == null)
        {
            this.channels_power = new double[16];
        }

        this.channels_peak[channel] = peak;
        this.channels_power[channel] = power;
    }
}
