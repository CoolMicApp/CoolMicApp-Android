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
#include <jni.h>
#include "coolmic-dsp/simple.h"
#include "coolmic-dsp/shout.h"
#include "coolmic-dsp/vumeter.h"
#include <android/log.h>


#define LOG_TAG "wrapper.c"
#define LOGI(x...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,x)

#ifdef __cplusplus
extern "C" {
#endif

coolmic_simple_t * coolmic_simple_obj;
static JavaVM *g_vm = NULL;
jclass vumeter_result_class;
jobject callbackHandlerObject;
jmethodID callbackHandlerMethod;
jmethodID callbackHandlerVUMeterMethod;
jmethodID callbackHandlerConnectionStateMethod;

JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_start(JNIEnv * env, jobject obj)
{
    LOGI("start start");
    return coolmic_simple_start(coolmic_simple_obj);
}

JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_stop(JNIEnv * env, jobject obj)
{
    LOGI("start stop");
    return coolmic_simple_stop(coolmic_simple_obj);
}

JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_ref(JNIEnv * env, jobject obj)
{
    LOGI("start ref");
    return coolmic_simple_ref(coolmic_simple_obj);
}

JNIEXPORT jint JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_unref(JNIEnv * env, jobject obj)
{
    LOGI("start unref");
    return coolmic_simple_unref(coolmic_simple_obj);
}

static void javaCallback(int val) {
    JNIEnv * env;
	// double check it's all ok
    int getEnvStat = (*g_vm)->GetEnv(g_vm,  (void **) &env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED) {
        LOGI("GetEnv: not attached");
        if ((*g_vm)->AttachCurrentThread(g_vm, &env, NULL) != 0) {
            LOGI("Failed to attach");
            return;
        }
    } else if (getEnvStat == JNI_OK) {
        //
    } else if (getEnvStat == JNI_EVERSION) {
	    LOGI("GetEnv: version not supported");

	    return;
    }

    LOGI("callback(%d)", val);

    (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, val);

	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}

	(*g_vm)->DetachCurrentThread(g_vm);
}

static void javaCallbackVUMeter(coolmic_vumeter_result_t * result) {
    JNIEnv * env;
    // double check it's all ok
    int getEnvStat = (*g_vm)->GetEnv(g_vm,  (void **) &env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED) {
        LOGI("GetEnv: not attached");
        if ((*g_vm)->AttachCurrentThread(g_vm, &env, NULL) != 0) {
            LOGI("Failed to attach");
            return;
        }
    } else if (getEnvStat == JNI_OK) {
        //
    } else if (getEnvStat == JNI_EVERSION) {
        LOGI("GetEnv: version not supported");
        return;
    }

    LOGI("VUM: PRE OBJ CONSTRUCTOR RESOLVING ");

    jmethodID methodId = (*env)->GetMethodID(env, vumeter_result_class, "<init>", "()V");

    LOGI("VUM: PRE OBJ GEN");

    jobject obj = (*env)->NewObject(env, vumeter_result_class, methodId);

    LOGI("VUM: PRE OBJ FILLING ");

    LOGI("VUM: PRE OBJ FILLING RATE");
    jfieldID fid = (*env)->GetFieldID(env, vumeter_result_class, "rate","I");
    (*env)->SetIntField(env, obj, fid, result->rate);

    LOGI("VUM: PRE OBJ FILLING CHANNELS");
    fid = (*env)->GetFieldID(env, vumeter_result_class, "channels","I");
    (*env)->SetIntField(env, obj, fid, result->channels);

    LOGI("VUM: PRE OBJ FILLING FRAMES");
    fid = (*env)->GetFieldID(env, vumeter_result_class, "frames","J");
    (*env)->SetLongField(env, obj, fid, result->frames);

    LOGI("VUM: PRE OBJ FILLING GLOBAL_PEAK");
    fid = (*env)->GetFieldID(env, vumeter_result_class, "global_peak","I");
    (*env)->SetIntField(env, obj, fid, result->global_peak);

    LOGI("VUM: PRE OBJ FILLING GLOBAL_POWER ");
    fid = (*env)->GetFieldID(env, vumeter_result_class, "global_power","D");
    (*env)->SetDoubleField(env, obj, fid, result->global_power);

    LOGI("VUM: POST OBJ GEN & FILLING ");

    (*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerVUMeterMethod, (*env)->NewGlobalRef(env, obj));


    LOGI("VUM: POST CALLBACK DISPATCH");

    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
    }

    (*g_vm)->DetachCurrentThread(g_vm);
}

static int callback(coolmic_simple_t *inst, void *userdata, coolmic_simple_event_t event, void *thread, void *arg0, void *arg1)
{
    LOGI("EVENT: %d %p %p", event, arg0, arg1);

    if(event == COOLMIC_SIMPLE_EVENT_THREAD_START)
    {
        LOGI("THREAD START!");
    }
    else if(event == COOLMIC_SIMPLE_EVENT_THREAD_POST_START)
    {
        LOGI("THREAD POST START!");

        javaCallback(1);
    }
    else if(event == COOLMIC_SIMPLE_EVENT_THREAD_PRE_STOP)
    {
        LOGI("THREAD PRE STOP");

        javaCallback(2);
    }
    else if(event == COOLMIC_SIMPLE_EVENT_THREAD_STOP)
    {
        LOGI("THREAD STOP");
    }
    else if(event == COOLMIC_SIMPLE_EVENT_ERROR)
    {
        javaCallback(3);
        LOGI("ERROR: %p", arg0);
    }

    else if(event == COOLMIC_SIMPLE_EVENT_VUMETER_RESULT)
    {
        return;
        coolmic_vumeter_result_t * result = (coolmic_vumeter_result_t*) arg0;

        LOGI("VUM: PRE CALL");
        javaCallbackVUMeter(result);
        LOGI("VUM: POST CALL ");

        LOGI("VUM: c%d c0 %f c1 %f c2 %f g %f", result->channels, result->channel_power[0], result->channel_power[1], result->channel_power[2], result->global_power);
    }
    else if(event == COOLMIC_SIMPLE_EVENT_STREAMSTATE)
    {
        coolmic_simple_connectionstate_t * state = (coolmic_simple_connectionstate_t *) arg0;
        const int * error_code = (const int*) arg1;

        if(error_code == NULL)
        {
            LOGI("SS: state: %d code: NULL", (int)*state);
        }
        else
        {
            LOGI("SS: state: %d code: %d", (int)*state, *error_code);
        }
    }
    else
    {
        LOGI("UNKNOWN EVENT: %d %p %p", event, arg0, arg1);
    }
}

JNIEXPORT void JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_init(JNIEnv * env, jobject obj, jobject objHandler, jstring hostname, jint port, jstring username, jstring password, jstring mount, jstring codec, jint rate, jint channels, jint buffersize)
{
    LOGI("start init");
    const char *codecNative    = (*env)->GetStringUTFChars(env, codec,    0);
    const char *hostnameNative = (*env)->GetStringUTFChars(env, hostname, 0);
    const char *usernameNative = (*env)->GetStringUTFChars(env, username, 0);
    const char *passwordNative = (*env)->GetStringUTFChars(env, password, 0);
    const char *mountNative    = (*env)->GetStringUTFChars(env, mount,    0);

    if (!(*env)->GetJavaVM(env, &g_vm) < 0)
        return;

    callbackHandlerObject = (*env)->NewGlobalRef(env, objHandler);

    jclass cls = (*env)->GetObjectClass(env, callbackHandlerObject);
    callbackHandlerMethod = (*env)->GetMethodID(env, cls, "callbackHandler", "(I)V");
    callbackHandlerVUMeterMethod = (*env)->GetMethodID(env, cls, "callbackVUMeterHandler", "(Lcc/echonet/coolmicdspjava/VUMeterResult;)V");

    if (callbackHandlerMethod == 0)
        return;

    coolmic_shout_config_t shout_config;

    memset(&shout_config, 0, sizeof(shout_config));

    shout_config.hostname = hostnameNative;
    shout_config.port     = port;
    shout_config.username = usernameNative;
    shout_config.password = passwordNative;
    shout_config.mount    = mountNative;

    coolmic_simple_obj = coolmic_simple_new(codecNative, rate, channels, buffersize, &shout_config);

    if(coolmic_simple_obj == NULL)
    {
        LOGI("error during init");
    }
    else
    {
        LOGI("everything okey");
    }

    coolmic_simple_set_callback(coolmic_simple_obj, callback, 0);

    (*env)->ReleaseStringUTFChars(env, codec, codecNative);
    (*env)->ReleaseStringUTFChars(env, codec, hostnameNative);
    (*env)->ReleaseStringUTFChars(env, codec, usernameNative);
    (*env)->ReleaseStringUTFChars(env, codec, passwordNative);
    (*env)->ReleaseStringUTFChars(env, codec, mountNative);

    LOGI("end init");
}

JNIEXPORT void JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_initNative(JNIEnv * env, jobject obj)
{
    vumeter_result_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "cc/echonet/coolmicdspjava/VUMeterResult"));
}

#ifdef __cplusplus
}
#endif
