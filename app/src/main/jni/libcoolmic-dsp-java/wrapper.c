/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
 * This file is part of Cool Mic.
 * 
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <string.h>
#include <jni.h>
#include "coolmic-dsp/coolmic-dsp.h"
#include "coolmic-dsp/simple.h"
#include "coolmic-dsp/shout.h"
#include "coolmic-dsp/vumeter.h"
#include "coolmic-dsp/util.h"
#include "coolmic-dsp/logging.h"
#include <android/log.h>


#define LOG_TAG "wrapper.c"
#define LOGI(x...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,x)

#ifdef __cplusplus
extern "C" {
#endif

coolmic_simple_t * coolmic_simple_obj = NULL;
static JavaVM *g_vm = NULL;
jclass vumeter_result_class;
jclass wrapper_callback_events_class;
jobject callbackHandlerObject;
jmethodID callbackHandlerMethod;
jmethodID callbackHandlerVUMeterMethod;

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_start(JNIEnv * env, jobject obj)
{
    LOGI("start start");

    if(coolmic_simple_obj == NULL)
    {
        LOGI("start bailing - no core obj");
        return -999666;
    }

    return coolmic_simple_start(coolmic_simple_obj);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_stop(JNIEnv * env, jobject obj)
{
    LOGI("start stop");

    if(coolmic_simple_obj == NULL)
    {
        LOGI("stop bailing - no core obj");
        return -999666;
    }


    return coolmic_simple_stop(coolmic_simple_obj);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_ref(JNIEnv * env, jobject obj)
{
    LOGI("start ref");

    if(coolmic_simple_obj == NULL)
    {
        LOGI("ref bailing - no core obj");
        return -999666;
    }

    return coolmic_simple_ref(coolmic_simple_obj);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_unref(JNIEnv * env, jobject obj)
{
    LOGI("start unref");

    if(coolmic_simple_obj == NULL)
    {
        LOGI("unref bailing - no core obj");
        return -999666;
    }

    int error =  coolmic_simple_unref(coolmic_simple_obj);

    if(error == COOLMIC_ERROR_NONE)
    {
        coolmic_simple_obj = NULL;
    }

    return error;
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jboolean JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_hasCore(JNIEnv * env, jobject obj)
{
    LOGI("hasCore %p %d", coolmic_simple_obj,  (coolmic_simple_obj == NULL ? 0 : 1));

    return (jboolean) (coolmic_simple_obj == NULL ? 0 : 1);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
static int callback(coolmic_simple_t *inst, void *userdata, coolmic_simple_event_t event, void *thread, void *arg0, void *arg1)
{
    coolmic_vumeter_result_t * vumeter_result;
    coolmic_simple_connectionstate_t * connection_state;
    struct timespec * timespecPtr;
    const int * error_code;

    JNIEnv * env;
    // double check it's all ok
    int getEnvStat = (*g_vm)->GetEnv(g_vm,  (void **) &env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED) {
        LOGI("GetEnv: not attached");
        if ((*g_vm)->AttachCurrentThread(g_vm, &env, NULL) != 0) {
            LOGI("Failed to attach");
            return 0 ;
        }
    } else if (getEnvStat == JNI_OK) {
        //
    } else if (getEnvStat == JNI_EVERSION) {
        LOGI("GetEnv: version not supported");

        return 0;
    }

    LOGI("EVENT: %d %p %p", event, arg0, arg1);

    jfieldID fidERROR    = (*env)->GetStaticFieldID(env, wrapper_callback_events_class , "ERROR", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject ERROR = (*env)->GetStaticObjectField(env, wrapper_callback_events_class, fidERROR);
    jfieldID fidThreadPostStart    = (*env)->GetStaticFieldID(env, wrapper_callback_events_class , "THREAD_POST_START", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_POST_START = (*env)->GetStaticObjectField(env, wrapper_callback_events_class, fidThreadPostStart);
    jfieldID fidThreadPreStop   = (*env)->GetStaticFieldID(env, wrapper_callback_events_class , "THREAD_PRE_STOP", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_PRE_STOP = (*env)->GetStaticObjectField(env, wrapper_callback_events_class, fidThreadPreStop);
    jfieldID fidThreadPostStop    = (*env)->GetStaticFieldID(env, wrapper_callback_events_class , "THREAD_POST_STOP", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_POST_STOP = (*env)->GetStaticObjectField(env, wrapper_callback_events_class, fidThreadPostStop);
    jmethodID methodId = (*env)->GetMethodID(env, vumeter_result_class, "<init>", "(IIJIDII)V");
    jmethodID vumeterResultChannelValues = (*env)->GetMethodID(env, vumeter_result_class, "setChannelPeakPower", "(IIDII)V");
    jfieldID fidSTREAMSTATE    = (*env)->GetStaticFieldID(env, wrapper_callback_events_class , "STREAMSTATE", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject STREAMSTATE = (*env)->GetStaticObjectField(env, wrapper_callback_events_class, fidSTREAMSTATE);
    jfieldID fidRECONNECT    = (*env)->GetStaticFieldID(env, wrapper_callback_events_class , "RECONNECT", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject RECONNECT = (*env)->GetStaticObjectField(env, wrapper_callback_events_class, fidRECONNECT);

    switch (event) {
        case COOLMIC_SIMPLE_EVENT_THREAD_START:
            LOGI("THREAD START!");
            break;
        case COOLMIC_SIMPLE_EVENT_THREAD_POST_START:
            LOGI("THREAD POST START!");

            (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, THREAD_POST_START, -1, -1);
            
            break;
        case COOLMIC_SIMPLE_EVENT_THREAD_PRE_STOP:
            LOGI("THREAD PRE STOP");

            (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, THREAD_PRE_STOP, -1, -1);


            (*env)->DeleteLocalRef(env, THREAD_POST_START);
            (*env)->DeleteLocalRef(env, THREAD_PRE_STOP);
            (*env)->DeleteLocalRef(env, THREAD_POST_STOP);
            (*env)->DeleteLocalRef(env, STREAMSTATE);
            (*env)->DeleteLocalRef(env, RECONNECT);
            (*env)->DeleteLocalRef(env, ERROR);

            if (getEnvStat == JNI_OK) {
                (*g_vm)->DetachCurrentThread(g_vm);
            }

            return 0;

            break;
        case COOLMIC_SIMPLE_EVENT_THREAD_STOP:
            LOGI("THREAD STOP");

            (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, THREAD_POST_STOP, -1, -1);

            break;
        case COOLMIC_SIMPLE_EVENT_ERROR:

            if (arg0 == NULL) {
                (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, ERROR, COOLMIC_ERROR_GENERIC, -1);
            } else {
                (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, ERROR, *(const int *) arg0, -1);
            }

            LOGI("ERROR: %p", arg0);
            break;
        case COOLMIC_SIMPLE_EVENT_VUMETER_RESULT:
            vumeter_result = (coolmic_vumeter_result_t *) arg0;

            jobject obj = (*env)->NewObject(env, vumeter_result_class, methodId, (jint) vumeter_result->rate, (jint) vumeter_result->channels, (jlong) vumeter_result->frames, (jint) vumeter_result->global_peak, (jdouble) vumeter_result->global_power, (jint) coolmic_util_ahsv2argb(1, coolmic_util_peak2hue(vumeter_result->global_peak, COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75), (jint) coolmic_util_ahsv2argb(1, coolmic_util_power2hue(vumeter_result->global_power, COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75));

            for(int i = 0;i < vumeter_result->channels; i++)
            {
                (*env)->CallVoidMethod(env, obj, vumeterResultChannelValues, (jint) i, (jint) vumeter_result->channel_peak[i], (jdouble) vumeter_result->channel_power[i], (jint) coolmic_util_ahsv2argb(1, coolmic_util_peak2hue(vumeter_result->channel_peak[i], COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75), (jint) coolmic_util_ahsv2argb(1, coolmic_util_power2hue(vumeter_result->channel_power[i], COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75));
            }

            (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerVUMeterMethod, (*env)->NewGlobalRef(env, obj));


            (*env)->DeleteLocalRef(env, obj);

            LOGI("VUM: c%d c0 %f c1 %f c2 %f g %f", vumeter_result->channels, vumeter_result->channel_power[0],
                 vumeter_result->channel_power[1], vumeter_result->channel_power[2], vumeter_result->global_power);

            break;
        case COOLMIC_SIMPLE_EVENT_STREAMSTATE:
            connection_state = (coolmic_simple_connectionstate_t *) arg0;
            error_code = (const int *) arg1;

            if (error_code == NULL)
            {
                (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, STREAMSTATE, (int) *connection_state, COOLMIC_ERROR_NONE);
                
                LOGI("SS: state: %d code: NULL", (int) *connection_state);
            }
            else
            {
                (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, STREAMSTATE, (int) *connection_state, *error_code);

                LOGI("SS: state: %d code: %d", (int) *connection_state, *error_code);
            }
            break;
        case COOLMIC_SIMPLE_EVENT_RECONNECT:
            timespecPtr = (struct timespec *) arg0;

            LOGI("RECONNECT: in: %lli", (long long int) timespecPtr->tv_sec);
            
            (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, RECONNECT, (int) timespecPtr->tv_sec, NULL);

            break;
        default:
            LOGI("UNKNOWN EVENT: %d %p %p", event, arg0, arg1);

            break;
    }

    (*env)->DeleteLocalRef(env, THREAD_POST_START);
    (*env)->DeleteLocalRef(env, THREAD_PRE_STOP);
    (*env)->DeleteLocalRef(env, THREAD_POST_STOP);
    (*env)->DeleteLocalRef(env, STREAMSTATE);
    (*env)->DeleteLocalRef(env, RECONNECT);
    (*env)->DeleteLocalRef(env, ERROR);

    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
    }

    //(*g_vm)->DetachCurrentThread(g_vm);

    return 0;
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT int JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_performMetaDataQualityUpdate(JNIEnv * env, jobject obj, jstring title, jstring artist, jdouble quality, jint restart)
{
    LOGI("performMetaDataQualityUpdate start");

    if(coolmic_simple_obj == NULL)
    {
        LOGI("performMetaDataQualityUpdate bailing - no core obj");
        return -999666;
    }

    const char *titleNative  = (*env)->GetStringUTFChars(env, title,    0);
    const char *artistNative = (*env)->GetStringUTFChars(env, artist, 0);
    int result = 0;

    LOGI("performMetaDataQualityUpdate(%s, %s, %g)", titleNative, artistNative, quality);

    coolmic_simple_set_meta(coolmic_simple_obj, "TITLE", titleNative, 1);
    coolmic_simple_set_meta(coolmic_simple_obj, "ARTIST", artistNative, 1);
    coolmic_simple_set_quality(coolmic_simple_obj, quality);

    if(restart)
    {
        result = coolmic_simple_restart_encoder(coolmic_simple_obj);
    }

    (*env)->ReleaseStringUTFChars(env, title, titleNative);
    (*env)->ReleaseStringUTFChars(env, artist, artistNative);

    return result;
}
#pragma clang diagnostic pop

static int logging_callback(coolmic_logging_level_t level, const char *msg)
{
    return 0;
    LOGI("libcoolmic: [%s] %s", coolmic_logging_level2string(level), msg);

    return 0;
}


#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT int JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_init(JNIEnv * env, jobject obj, jobject objHandler, jstring hostname, jint port, jstring username, jstring password, jstring mount, jstring codec, jint rate, jint channels, jint buffersize)
{
    LOGI("start init");

    if(coolmic_simple_obj != NULL)
    {
        LOGI("init bailing - previous core obj - %p", coolmic_simple_obj);
        return -666666;
    }

    const char *codecNative    = (*env)->GetStringUTFChars(env, codec,    0);
    const char *hostnameNative = (*env)->GetStringUTFChars(env, hostname, 0);
    const char *usernameNative = (*env)->GetStringUTFChars(env, username, 0);
    const char *passwordNative = (*env)->GetStringUTFChars(env, password, 0);
    const char *mountNative    = (*env)->GetStringUTFChars(env, mount,    0);

    if ((*env)->GetJavaVM(env, &g_vm) != 0)
        return -1;

    callbackHandlerObject = (*env)->NewGlobalRef(env, objHandler);

    jclass cls = (*env)->GetObjectClass(env, callbackHandlerObject);
    callbackHandlerMethod = (*env)->GetMethodID(env, cls, "callbackHandler", "(Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;II)V");
    callbackHandlerVUMeterMethod = (*env)->GetMethodID(env, cls, "callbackVUMeterHandler", "(Lcc/echonet/coolmicdspjava/VUMeterResult;)V");

    if (callbackHandlerMethod == 0)
        return -2;

    coolmic_logging_set_cb_simple(logging_callback);

    coolmic_shout_config_t shout_config;

    memset(&shout_config, 0, sizeof(shout_config));

    shout_config.hostname = hostnameNative;
    shout_config.port     = port;
    shout_config.username = usernameNative;
    shout_config.password = passwordNative;
    shout_config.mount    = mountNative;

    coolmic_simple_obj = coolmic_simple_new(codecNative, (uint_least32_t) (int) rate,
                                            (unsigned int) (int) channels, buffersize, &shout_config);

    if(coolmic_simple_obj == NULL)
    {
        LOGI("error during init");
    }
    else
    {
        LOGI("everything okey");

        coolmic_simple_set_callback(coolmic_simple_obj, callback, 0);
    }

    (*env)->ReleaseStringUTFChars(env, codec, codecNative);
    (*env)->ReleaseStringUTFChars(env, hostname, hostnameNative);
    (*env)->ReleaseStringUTFChars(env, username, usernameNative);
    (*env)->ReleaseStringUTFChars(env, password, passwordNative);
    (*env)->ReleaseStringUTFChars(env, mount, mountNative);

    LOGI("end init");

    return (coolmic_simple_obj == NULL ? 1 : 0);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT int JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_setVuMeterInterval(JNIEnv * env, jobject obj, jint interval)
{
    LOGI("setVuMeterInterval start");

    if(coolmic_simple_obj == NULL)
    {
        LOGI("setVuMeterInterval bailing - no core obj");
        return -999666;
    }

    return coolmic_simple_set_vumeter_interval(coolmic_simple_obj, (size_t) interval);
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic pop
JNIEXPORT void JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_initNative(JNIEnv * env, jobject obj)
{
    vumeter_result_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "cc/echonet/coolmicdspjava/VUMeterResult"));
    wrapper_callback_events_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "cc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents"));

}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_setReconnectionProfile(JNIEnv *env, jclass type,
                                                              jstring profile_) {
    const char *profile = (*env)->GetStringUTFChars(env, profile_, 0);

    if (coolmic_simple_obj == NULL)
    {
        LOGI("setReconnectionProfile bailing - no core obj");
        return -999666;
    }

    jint result = coolmic_simple_set_reconnection_profile(coolmic_simple_obj, profile);

    (*env)->ReleaseStringUTFChars(env, profile_, profile);

    return result;
}
#pragma clang diagnostic pop

static int __setMasterGain(unsigned int channels, uint16_t scale, const uint16_t *gain) {
    coolmic_transform_t *transform;
    int ret;

    if (coolmic_simple_obj == NULL)
    {
        LOGI("setMasterGain bailing - no core obj");
        return -999666;
    }

    transform = coolmic_simple_get_transform(coolmic_simple_obj);
    if (transform == NULL) {
        LOGI("setMasterGain bailing - no transform obj");
        return -999666;
    }

    if (channels > 1) {
        LOGI("gain: channels=%u, scale=%u, gain=%p{%u, %u, ...}", (unsigned int)channels, (unsigned int)scale, gain, (unsigned int)gain[0], (unsigned int)gain[1]);
    } else {
        LOGI("gain: channels=%u, scale=%u, gain=%p{%u, ...}", (unsigned int)channels, (unsigned int)scale, gain, (unsigned int)gain[0]);
    }
    ret = coolmic_transform_set_master_gain(transform, channels, scale, gain);

    coolmic_transform_unref(transform);

    return ret;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_resetMasterGain(JNIEnv *env, jclass type) {
    return __setMasterGain(0, 0, NULL);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_setMasterGainStereo(JNIEnv *env, jclass type, jint scale,
                                                           jint gain_left, jint gain_right) {
    const uint16_t cgain[2] = {(uint16_t)gain_left, (uint16_t)gain_right};

    return __setMasterGain(2, (uint16_t)scale, cgain);
}
#pragma clang diagnostic pop

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_setMasterGainMono(JNIEnv *env, jclass type, jint scale,
                                                         jint gain) {
    const uint16_t cgain[1] = {(uint16_t)gain};

    return __setMasterGain(1, (uint16_t)scale, cgain);
}
#pragma clang diagnostic pop

#ifdef __cplusplus
}
#endif
