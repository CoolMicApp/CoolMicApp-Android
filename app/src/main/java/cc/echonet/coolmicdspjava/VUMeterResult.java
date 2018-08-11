package cc.echonet.coolmicdspjava;

import java.io.Serializable;

/**
 * Created by stephanj on 23.05.2016.
 */
public class VUMeterResult implements Serializable {

    public VUMeterResult(int rate, int channels, long frames, int global_peak, double global_power, int global_peak_color, int global_power_color) {
        this.rate = rate;
        this.channels = channels;
        this.frames = frames;
        this.global_peak = global_peak;
        this.global_power = global_power;
        this.global_peak_color = global_peak_color;
        this.global_power_color = global_power_color;
    }

    public int rate;
    public int channels;

    public long frames;

    public int global_peak;
    public double global_power;

    public int[] channels_peak;
    public double[] channels_power;

    public int global_peak_color;
    public int global_power_color;

    public int[] channels_peak_color;
    public int[] channels_power_color;

    @SuppressWarnings("unused")
    public void setChannelPeakPower(int channel, int peak, double power, int peak_color, int power_color) {
        if (this.channels_peak == null) {
            this.channels_peak = new int[16];
        }

        if (this.channels_power == null) {
            this.channels_power = new double[16];
        }

        if (this.channels_peak_color == null) {
            this.channels_peak_color = new int[16];
        }

        if (this.channels_power_color == null) {
            this.channels_power_color = new int[16];
        }

        this.channels_peak[channel] = peak;
        this.channels_power[channel] = power;

        this.channels_peak_color[channel] = peak_color;
        this.channels_power_color[channel] = power_color;
    }
}
