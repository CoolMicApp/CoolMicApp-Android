#include <jni.h>
#include "libcoolmic-dsp/simple.h"
#include "libcoolmic-dsp/shout.h"
#include <android/log.h>


#define LOG_TAG "Wrapper"
#define LOGI(x...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,x)

#ifdef __cplusplus
extern "C" {
#endif

coolmic_simple_t * coolmic_simple_obj;


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

JNIEXPORT void JNICALL Java_cc_echonet_coolmicdspjava_Wrapper_init(JNIEnv * env, jobject obj, jstring codec, jint rate, jint channels)
{
    LOGI("start init");
    const char *codecNative = (*env)->GetStringUTFChars(env, codec, 0);

    coolmic_shout_config_t shout_config;

    shout_config.hostname = "source.echonet.cc";
    shout_config.port     = 8000;
    shout_config.username = "test";
    shout_config.password = "test123";
    shout_config.mount    = "test.ogg";

    coolmic_simple_obj = coolmic_simple_new(codecNative, rate, channels, -1, &shout_config);

    if(coolmic_simple_obj == NULL)
    {
        LOGI("error during init");
    }
    else
    {
        LOGI("everything okey");
    }


    (*env)->ReleaseStringUTFChars(env, codec, codecNative);
    LOGI("end init");
}

#ifdef __cplusplus
}
#endif
