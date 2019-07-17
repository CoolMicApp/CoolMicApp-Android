package cc.echonet.coolmicapp.BackgroundService.Client;

import cc.echonet.coolmicapp.BackgroundService.State;
import cc.echonet.coolmicdspjava.VUMeterResult;

public interface EventListener {
    void onBackgroundServiceState(State state);
    void onBackgroundServiceError(/*TODO*/);
    void onBackgroundServiceStartRecording();
    void onBackgroundServiceStopRecording();
    void onBackgroundServicePermissionsMissing();
    void onBackgroundServiceConnectionUnset(); /* TODO: ??? */
    void onBackgroundServiceCMTSTOSAcceptMissing();
    void onBackgroundServiceVUMeterUpdate(VUMeterResult result);
    void onBackgroundServiceGainUpdate(int left, int right);
}
