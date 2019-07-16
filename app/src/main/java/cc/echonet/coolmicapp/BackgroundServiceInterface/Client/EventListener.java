package cc.echonet.coolmicapp.BackgroundServiceInterface.Client;

import cc.echonet.coolmicapp.BackgroundServiceState;
import cc.echonet.coolmicdspjava.VUMeterResult;

public interface EventListener {
    void onBackgroundServiceState(BackgroundServiceState state);
    void onBackgroundServiceError(/*TODO*/);
    void onBackgroundServiceStartRecording();
    void onBackgroundServiceStopRecording();
    void onBackgroundServicePermissionsMissing();
    void onBackgroundServiceConnectionUnset(); /* TODO: ??? */
    void onBackgroundServiceCMTSTOSAcceptMissing();
    void onBackgroundServiceVUMeterUpdate(VUMeterResult result);
    void onBackgroundServiceGainUpdate(int left, int right);
}
