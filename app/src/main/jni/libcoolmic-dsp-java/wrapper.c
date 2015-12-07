#include <jni.h>
#include "libcoolmic-dsp/simple.h"
#include "libcoolmic-dsp/shout.h"
#include <android/log.h>


#define LOG_TAG "wrapper.c"
#define LOGI(x...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,x)

#ifdef __cplusplus
extern "C" {
#endif

coolmic_simple_t * coolmic_simple_obj;
static JavaVM *g_vm = NULL;
jobject callbackHandlerObject;
jmethodID callbackHandlerMethod;


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

void javaCallback(int val) {
	JNIEnv * env;
	// double check it's all ok
	int getEnvStat = (*g_vm)->GetEnv(g_vm,  (void **) &env, JNI_VERSION_1_6);
	if (getEnvStat == JNI_EDETACHED) {
		LOGI("GetEnv: not attached");
		if ((*g_vm)->AttachCurrentThread(g_vm, &env, NULL) != 0) {
			LOGI("Failed to attach");
		}
	} else if (getEnvStat == JNI_OK) {
		//
	} else if (getEnvStat == JNI_EVERSION) {
		LOGI("GetEnv: version not supported");
	}

	(*env)->CallVoidMethod(env, callbackHandlerObject, callbackHandlerMethod, val);

	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}

	(*g_vm)->DetachCurrentThread(g_vm);
}

int callback(coolmic_simple_t *inst, void *userdata, coolmic_simple_event_t event, void *thread, void *arg0, void *arg1)
{
    LOGI("EVENT: %d", event);

    if(event == COOLMIC_SIMPLE_EVENT_THREAD_POST_START)
    {
        LOGI("THREAD START!");
        /*
        (*vm)->AttachCurrentThread(vm, &env, NULL);
        */
        javaCallback(1);
    }
    else if(event == COOLMIC_SIMPLE_EVENT_THREAD_PRE_STOP)
    {
        LOGI("THREAD STOP");
        javaCallback(2);
        /*
        int check = (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);
        LOGI("Detach destructor ==: %d", check);
        if (check != JNI_EDETACHED) {

            javaCallback(2);

            int detach = (*vm)->DetachCurrentThread(vm);
        }
        */
    }
    else if(event == COOLMIC_SIMPLE_EVENT_ERROR)
    {
        javaCallback(3);
        LOGI("ERROR: %d",(const int )arg0);
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

#ifdef __cplusplus
}
#endif
