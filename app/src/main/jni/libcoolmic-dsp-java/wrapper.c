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
#include "coolmic-dsp/simple-segment.h"
#include "coolmic-dsp/shout.h"
#include "coolmic-dsp/vumeter.h"
#include "coolmic-dsp/util.h"
#include "coolmic-dsp/logging.h"
#include "InputStreamAdapter.h"
#include <igloo/types.h>
#include <android/log.h>
#include <igloo/ro.h>
#include <malloc.h>


#define LOG_TAG "wrapper.c"
#define LOGI(x...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,x)

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    coolmic_simple_t * coolmic_simple_obj;
    JavaVM *g_vm;
    jclass vumeter_result_class;
    jclass wrapper_callback_events_class;
    jobject callbackHandlerObject;
    jmethodID callbackHandlerMethod;
    jmethodID callbackHandlerVUMeterMethod;
} wrapper_t;

static wrapper_t *get_wrapper_t(JNIEnv *env, jobject obj) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fidNativeObject = (*env)->GetFieldID(env, cls, "nativeObject", "J");
    wrapper_t * wrapper = (void*)(jlong)(*env)->GetLongField(env, obj, fidNativeObject);

    LOGI("wrapper=%p", wrapper);

    if (wrapper == NULL) {
        wrapper = calloc(1, sizeof(wrapper_t));
        wrapper->vumeter_result_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "cc/echonet/coolmicdspjava/VUMeterResult"));
        wrapper->wrapper_callback_events_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "cc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents"));
        (*env)->SetLongField(env, obj, fidNativeObject, wrapper);
    }

    return wrapper;
}

static void free_wrapper_t(JNIEnv *env, jobject obj, wrapper_t ** wrapper) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fidNativeObject = (*env)->GetFieldID(env, cls, "nativeObject", "J");

    igloo_ro_unref((*wrapper)->coolmic_simple_obj);
    (*env)->DeleteGlobalRef(env, (*wrapper)->callbackHandlerObject);

    free(*wrapper);
    *wrapper = NULL;

    (*env)->SetLongField(env, obj, fidNativeObject, 0);
}

JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_start(JNIEnv * env, jobject obj)
{
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    LOGI("start start");

    if(wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("start bailing - no core obj");
        return -999666;
    }

    return coolmic_simple_start(wrapper->coolmic_simple_obj);
}

JNIEXPORT void JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_close(JNIEnv * env, jobject obj)
{
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    LOGI("start unref");

    if(wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("unref bailing - no core obj");
    }

    free_wrapper_t(env, obj, &wrapper);
}

JNIEXPORT jboolean JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_isPrepared(JNIEnv * env, jobject obj)
{
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    LOGI("hasCore %p %d", wrapper->coolmic_simple_obj,  (wrapper->coolmic_simple_obj == NULL ? 0 : 1));

    return (jboolean) (wrapper->coolmic_simple_obj == NULL ? 0 : 1);
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
static int callback(coolmic_simple_t *inst, void *userdata, coolmic_simple_event_t event, void *thread, void *arg0, void *arg1)
{
    wrapper_t * wrapper = userdata;
    coolmic_vumeter_result_t * vumeter_result;
    coolmic_simple_connectionstate_t * connection_state;
    coolmic_simple_segment_pipeline_t pipeline;
    struct timespec * timespecPtr;
    const int * error_code;

    JNIEnv * env;
    // double check it's all ok
    int getEnvStat = (*(wrapper->g_vm))->GetEnv(wrapper->g_vm,  (void **) &env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED) {
        LOGI("GetEnv: not attached");
        if ((*(wrapper->g_vm))->AttachCurrentThread(wrapper->g_vm, &env, NULL) != 0) {
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

    jfieldID fidERROR    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "ERROR", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject ERROR = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidERROR);
    jfieldID fidThreadPostStart    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "THREAD_POST_START", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_POST_START = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidThreadPostStart);
    jfieldID fidThreadPreStop   = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "THREAD_PRE_STOP", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_PRE_STOP = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidThreadPreStop);
    jfieldID fidThreadPostStop    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "THREAD_POST_STOP", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_POST_STOP = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidThreadPostStop);
    jfieldID fidThreadStop    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "THREAD_STOP", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject THREAD_STOP = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidThreadStop);
    jmethodID methodId = (*env)->GetMethodID(env, wrapper->vumeter_result_class, "<init>", "(IIJIDII)V");
    jmethodID vumeterResultChannelValues = (*env)->GetMethodID(env, wrapper->vumeter_result_class, "setChannelPeakPower", "(IIDII)V");
    jfieldID fidSTREAMSTATE    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "STREAMSTATE", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject STREAMSTATE = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidSTREAMSTATE);
    jfieldID fidRECONNECT    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "RECONNECT", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject RECONNECT = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidRECONNECT);
    jfieldID fidSEGMENT_CONNECT    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "SEGMENT_CONNECT", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject SEGMENT_CONNECT = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidSEGMENT_CONNECT);
    jfieldID fidSEGMENT_DISCONNECT    = (*env)->GetStaticFieldID(env, wrapper->wrapper_callback_events_class , "SEGMENT_DISCONNECT", "Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;");
    jobject SEGMENT_DISCONNECT = (*env)->GetStaticObjectField(env, wrapper->wrapper_callback_events_class, fidSEGMENT_DISCONNECT);

    switch (event) {
        case COOLMIC_SIMPLE_EVENT_THREAD_START:
            LOGI("THREAD START!");
            break;
        case COOLMIC_SIMPLE_EVENT_THREAD_POST_START:
            LOGI("THREAD POST START!");

            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, THREAD_POST_START, -1, -1);
            
            break;
        case COOLMIC_SIMPLE_EVENT_THREAD_PRE_STOP:
            LOGI("THREAD PRE STOP");

            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, THREAD_PRE_STOP, -1, -1);


            (*env)->DeleteLocalRef(env, THREAD_POST_START);
            (*env)->DeleteLocalRef(env, THREAD_PRE_STOP);
            (*env)->DeleteLocalRef(env, THREAD_POST_STOP);
            (*env)->DeleteLocalRef(env, THREAD_STOP);
            (*env)->DeleteLocalRef(env, STREAMSTATE);
            (*env)->DeleteLocalRef(env, RECONNECT);
            (*env)->DeleteLocalRef(env, SEGMENT_CONNECT);
            (*env)->DeleteLocalRef(env, SEGMENT_DISCONNECT);
            (*env)->DeleteLocalRef(env, ERROR);

            if (getEnvStat == JNI_OK) {
                (*(wrapper->g_vm))->DetachCurrentThread(wrapper->g_vm);
            }

            return 0;

            break;
        case COOLMIC_SIMPLE_EVENT_THREAD_STOP:
            LOGI("THREAD STOP");

            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, THREAD_POST_STOP, -1, -1);

            break;
        case COOLMIC_SIMPLE_EVENT_ERROR:

            if (arg0 == NULL) {
                (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, ERROR, COOLMIC_ERROR_GENERIC, -1);
            } else {
                (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, ERROR, *(const int *) arg0, -1);
            }

            LOGI("ERROR: %p", arg0);
            break;
        case COOLMIC_SIMPLE_EVENT_VUMETER_RESULT:
            vumeter_result = (coolmic_vumeter_result_t *) arg0;

            jobject obj = (*env)->NewObject(env, wrapper->vumeter_result_class, methodId, (jint) vumeter_result->rate, (jint) vumeter_result->channels, (jlong) vumeter_result->frames, (jint) vumeter_result->global_peak, (jdouble) vumeter_result->global_power, (jint) coolmic_util_ahsv2argb(1, coolmic_util_peak2hue(vumeter_result->global_peak, COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75), (jint) coolmic_util_ahsv2argb(1, coolmic_util_power2hue(vumeter_result->global_power, COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75));

            for(int i = 0;i < vumeter_result->channels; i++)
            {
                (*env)->CallVoidMethod(env, obj, vumeterResultChannelValues, (jint) i, (jint) vumeter_result->channel_peak[i], (jdouble) vumeter_result->channel_power[i], (jint) coolmic_util_ahsv2argb(1, coolmic_util_peak2hue(vumeter_result->channel_peak[i], COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75), (jint) coolmic_util_ahsv2argb(1, coolmic_util_power2hue(vumeter_result->channel_power[i], COOLMIC_UTIL_PROFILE_DEFAULT), 1.0, 0.75));
            }

            jobject ref = (*env)->NewGlobalRef(env, obj);

            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerVUMeterMethod, ref);

            (*env)->DeleteGlobalRef(env, ref);
            (*env)->DeleteLocalRef(env, obj);

            LOGI("VUM: c%d c0 %f c1 %f c2 %f g %f", vumeter_result->channels, vumeter_result->channel_power[0],
                 vumeter_result->channel_power[1], vumeter_result->channel_power[2], vumeter_result->global_power);

            break;
        case COOLMIC_SIMPLE_EVENT_STREAMSTATE:
            connection_state = (coolmic_simple_connectionstate_t *) arg0;
            error_code = (const int *) arg1;

            if (error_code == NULL)
            {
                (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, STREAMSTATE, (int) *connection_state, COOLMIC_ERROR_NONE);
                
                LOGI("SS: state: %d code: NULL", (int) *connection_state);
            }
            else
            {
                (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, STREAMSTATE, (int) *connection_state, *error_code);

                LOGI("SS: state: %d code: %d", (int) *connection_state, *error_code);
            }
            break;
        case COOLMIC_SIMPLE_EVENT_RECONNECT:
            timespecPtr = (struct timespec *) arg0;

            LOGI("RECONNECT: in: %lli", (long long int) timespecPtr->tv_sec);
            
            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, RECONNECT, (int) timespecPtr->tv_sec, NULL);

            break;
        case COOLMIC_SIMPLE_EVENT_SEGMENT_CONNECT:
            pipeline = *(coolmic_simple_segment_pipeline_t*)arg0;
            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, SEGMENT_CONNECT, pipeline == COOLMIC_SIMPLE_SP_LIVE, NULL);
            break;
        case COOLMIC_SIMPLE_EVENT_SEGMENT_DISCONNECT:
            pipeline = *(coolmic_simple_segment_pipeline_t*)arg0;
            (*env)->CallVoidMethod(env, wrapper->callbackHandlerObject, wrapper->callbackHandlerMethod, SEGMENT_DISCONNECT, pipeline == COOLMIC_SIMPLE_SP_LIVE, NULL);
            break;
        default:
            LOGI("UNKNOWN EVENT: %d %p %p", event, arg0, arg1);

            break;
    }

    (*env)->DeleteLocalRef(env, THREAD_POST_START);
    (*env)->DeleteLocalRef(env, THREAD_PRE_STOP);
    (*env)->DeleteLocalRef(env, THREAD_POST_STOP);
    (*env)->DeleteLocalRef(env, THREAD_STOP);
    (*env)->DeleteLocalRef(env, STREAMSTATE);
    (*env)->DeleteLocalRef(env, RECONNECT);
    (*env)->DeleteLocalRef(env, SEGMENT_CONNECT);
    (*env)->DeleteLocalRef(env, SEGMENT_DISCONNECT);
    (*env)->DeleteLocalRef(env, ERROR);

    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
    }

    //(*g_vm)->DetachCurrentThread(g_vm);

    return 0;
}
#pragma clang diagnostic pop

JNIEXPORT int JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_performMetaDataQualityUpdate(JNIEnv * env, jobject obj, jobjectArray track, jdouble quality, jint restart)
{
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    int trackCount, trackCursor;

    LOGI("performMetaDataQualityUpdate start");

    if(wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("performMetaDataQualityUpdate bailing - no core obj");
        return -999666;
    }

    int result = 0;

    coolmic_simple_set_quality(wrapper->coolmic_simple_obj, quality);

    trackCount = (*env)->GetArrayLength(env, track);
    for (trackCursor = 0; trackCursor < trackCount; trackCursor += 2) {
        jstring javaKey = (*env)->GetObjectArrayElement(env, track, trackCursor);
        jstring javaValue = (*env)->GetObjectArrayElement(env, track, trackCursor + 1);
        const char *key = (*env)->GetStringUTFChars(env, javaKey, NULL);
        const char *value = (*env)->GetStringUTFChars(env, javaValue, NULL);

        LOGI("track: %s -> %s", key, value);
        coolmic_simple_set_meta(wrapper->coolmic_simple_obj, key, value, 1);

        (*env)->ReleaseStringUTFChars(env, javaKey, key);
        (*env)->ReleaseStringUTFChars(env, javaValue, value);

        (*env)->DeleteLocalRef(env, javaKey);
        (*env)->DeleteLocalRef(env, javaValue);
    }

    if(restart)
    {
        result = coolmic_simple_restart_encoder(wrapper->coolmic_simple_obj);
    }

    return result;
}

static int logging_callback(coolmic_logging_level_t level, const char *msg)
{
    return 0;
    LOGI("libcoolmic: [%s] %s", coolmic_logging_level2string(level), msg);

    return 0;
}


JNIEXPORT int JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_prepare(JNIEnv * env, jobject obj, jobject objHandler, jstring hostname, jint port, jstring username, jstring password, jstring mount, jstring codec, jint rate, jint channels, jint buffersize, jstring software_name, jstring software_version, jstring software_comment, jobjectArray station)
{
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    const char *software_nameNative = NULL;
    const char *software_versionNative = NULL;
    const char *software_commentNative = NULL;
    int stationCount, stationCursor;

    LOGI("start init");

    if(wrapper->coolmic_simple_obj != NULL)
    {
        LOGI("init bailing - previous core obj - %p", wrapper->coolmic_simple_obj);
        return -666666;
    }

    const char *codecNative    = (*env)->GetStringUTFChars(env, codec,    NULL);
    const char *hostnameNative = (*env)->GetStringUTFChars(env, hostname, NULL);
    const char *usernameNative = (*env)->GetStringUTFChars(env, username, NULL);
    const char *passwordNative = (*env)->GetStringUTFChars(env, password, NULL);
    const char *mountNative    = (*env)->GetStringUTFChars(env, mount,    NULL);
    if (software_name)
        software_nameNative = (*env)->GetStringUTFChars(env, software_name, NULL);
    if (software_version)
        software_versionNative = (*env)->GetStringUTFChars(env, software_version, NULL);
    if (software_comment)
        software_commentNative = (*env)->GetStringUTFChars(env, software_comment, NULL);

    if ((*env)->GetJavaVM(env, &(wrapper->g_vm)) != 0)
        return -1;

    wrapper->callbackHandlerObject = (*env)->NewGlobalRef(env, objHandler);

    jclass cls = (*env)->GetObjectClass(env, wrapper->callbackHandlerObject);
    wrapper->callbackHandlerMethod = (*env)->GetMethodID(env, cls, "callbackHandler", "(Lcc/echonet/coolmicdspjava/WrapperConstants$WrapperCallbackEvents;II)V");
    wrapper->callbackHandlerVUMeterMethod = (*env)->GetMethodID(env, cls, "callbackVUMeterHandler", "(Lcc/echonet/coolmicdspjava/VUMeterResult;)V");

    if (wrapper->callbackHandlerMethod == 0)
        return -2;

    coolmic_logging_set_cb_simple(logging_callback);

    coolmic_shout_config_t shout_config;

    memset(&shout_config, 0, sizeof(shout_config));

    shout_config.hostname           = hostnameNative;
    shout_config.port               = port;
    shout_config.username           = usernameNative;
    shout_config.password           = passwordNative;
    shout_config.mount              = mountNative;
    shout_config.software_name      = software_nameNative;
    shout_config.software_version   = software_versionNative;
    shout_config.software_comment   = software_commentNative;

    wrapper->coolmic_simple_obj = coolmic_simple_new(NULL, igloo_RO_NULL, codecNative, (uint_least32_t) (int) rate,
                                            (unsigned int) (int) channels, buffersize, &shout_config);

    if(wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("error during init");
    }
    else
    {
        LOGI("everything okey");

        coolmic_simple_set_callback(wrapper->coolmic_simple_obj, callback, wrapper);
    }

    (*env)->ReleaseStringUTFChars(env, codec, codecNative);
    (*env)->ReleaseStringUTFChars(env, hostname, hostnameNative);
    (*env)->ReleaseStringUTFChars(env, username, usernameNative);
    (*env)->ReleaseStringUTFChars(env, password, passwordNative);
    (*env)->ReleaseStringUTFChars(env, mount, mountNative);
    if (software_name)
        (*env)->ReleaseStringUTFChars(env, software_name, software_nameNative);
    if (software_version)
        (*env)->ReleaseStringUTFChars(env, software_version, software_versionNative);
    if (software_comment)
        (*env)->ReleaseStringUTFChars(env, software_comment, software_commentNative);

    /*
     * 23:16 <@ph3-der-loewe>     int stringCount = env->GetArrayLength(stringArray);
23:16 <@ph3-der-loewe>     for (int i=0; i<stringCount; i++) {
23:16 <@ph3-der-loewe>         jstring string = (jstring) (env->GetObjectArrayElement(stringArray, i));

     */

    stationCount = (*env)->GetArrayLength(env, station);
    for (stationCursor = 0; stationCursor < stationCount; stationCursor += 2) {
        jstring javaKey = (*env)->GetObjectArrayElement(env, station, stationCursor);
        jstring javaValue = (*env)->GetObjectArrayElement(env, station, stationCursor + 1);
        const char *key = (*env)->GetStringUTFChars(env, javaKey, NULL);
        const char *value = (*env)->GetStringUTFChars(env, javaValue, NULL);

        LOGI("station: %s -> %s", key, value);
        coolmic_simple_set_station_meta(wrapper->coolmic_simple_obj, key, value);

        (*env)->ReleaseStringUTFChars(env, javaKey, key);
        (*env)->ReleaseStringUTFChars(env, javaValue, value);

        (*env)->DeleteLocalRef(env, javaKey);
        (*env)->DeleteLocalRef(env, javaValue);
    }

    LOGI("end init");

    return (wrapper->coolmic_simple_obj == NULL ? 1 : 0);
}

JNIEXPORT int JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_setVuMeterInterval(JNIEnv * env, jobject obj, jint interval)
{
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    LOGI("setVuMeterInterval start");

    if(wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("setVuMeterInterval bailing - no core obj");
        return -999666;
    }

    LOGI("Setting VU-Meter Interval to %i", (int)interval);

    return coolmic_simple_set_vumeter_interval(wrapper->coolmic_simple_obj, (size_t) interval);
}

JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_setReconnectionProfile(JNIEnv *env, jobject obj, jstring profile_) {
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    const char *profile = (*env)->GetStringUTFChars(env, profile_, 0);

    LOGI("setReconnectionProfile profile=%s", profile);

    if (wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("setReconnectionProfile bailing - no core obj");
        return -999666;
    }

    jint result = coolmic_simple_set_reconnection_profile(wrapper->coolmic_simple_obj, profile);

    (*env)->ReleaseStringUTFChars(env, profile_, profile);

    LOGI("setReconnectionProfile result=%i", (int)result);

    return result;
}

static int __setMasterGain(wrapper_t * wrapper, unsigned int channels, uint16_t scale, const uint16_t *gain) {
    coolmic_transform_t *transform;
    int ret;

    if (wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("setMasterGain bailing - no core obj");
        return -999666;
    }

    transform = coolmic_simple_get_transform(wrapper->coolmic_simple_obj);
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

    igloo_ro_unref(transform);

    return ret;
}

JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_setMasterGain__III(JNIEnv *env, jobject obj, jint scale,
                                                          jint gain_left, jint gain_right) {
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    const uint16_t cgain[2] = {(uint16_t)gain_left, (uint16_t)gain_right};

    return __setMasterGain(wrapper, 2, (uint16_t)scale, cgain);
}

JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_setMasterGain__II(JNIEnv *env, jobject obj, jint scale,
                                                         jint gain) {
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    const uint16_t cgain[1] = {(uint16_t)gain};

    return __setMasterGain(wrapper, 1, (uint16_t)scale, cgain);
}

JNIEXPORT jint JNICALL
Java_cc_echonet_coolmicdspjava_Wrapper_nextSegment(JNIEnv *env, jobject obj,
                                                   jobject input_stream_adapter) {
    wrapper_t * wrapper = get_wrapper_t(env, obj);
    coolmic_iohandle_t *io;
    coolmic_simple_segment_t *segment;

    if (wrapper->coolmic_simple_obj == NULL)
    {
        LOGI("nextSegment bailing - no core obj");
        return -999666;
    }

    if (input_stream_adapter) {
        io = inputStreamAdapter_new_iohandle(env, input_stream_adapter);

        LOGI("XXX new FILE_SIMPLE");
        segment = coolmic_simple_segment_new(NULL, igloo_RO_NULL, COOLMIC_SIMPLE_SP_FILE_SIMPLE, NULL, NULL, io);

        igloo_ro_unref(io);
    } else {
        LOGI("XXX new LIVE");
        segment = coolmic_simple_segment_new(NULL, igloo_RO_NULL, COOLMIC_SIMPLE_SP_LIVE, NULL, NULL, NULL);
    }

    coolmic_simple_queue_segment(wrapper->coolmic_simple_obj, segment);

    igloo_ro_unref(segment);

    coolmic_simple_switch_segment(wrapper->coolmic_simple_obj);

    return 0;
}

#ifdef __cplusplus
}
#endif
