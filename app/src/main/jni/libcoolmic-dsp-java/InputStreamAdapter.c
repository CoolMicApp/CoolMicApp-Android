//
// Created by phschafft on 2019/06/03.
//

#include "InputStreamAdapter.h"
#include <stdlib.h>
#include <string.h>

struct InputStreamAdapter {
    JavaVM *vm;
    jobject adapter;
    jmethodID callback;
};


InputStreamAdapter_t *    inputStreamAdapter_new(JNIEnv *env, jobject adapter) {
    InputStreamAdapter_t *self = calloc(1, sizeof(*self));
    jclass cls;

    if (!self)
        return NULL;


    if ((*env)->GetJavaVM(env, &(self->vm)) != 0) {
        free(self);
        return NULL;
    }

    self->adapter = (*env)->NewGlobalRef(env, adapter);

    if (!self->adapter) {
        free(self);
        return NULL;
    }

    cls = (*env)->GetObjectClass(env, adapter);
    self->callback = (*env)->GetMethodID(env, cls, "callback", "(Ljava/lang/String;[B)I");

    return self;
}

static int callback(InputStreamAdapter_t *self, const char *task, void *buf, size_t len)
{
    JNIEnv * env;
    int getEnvStat;
    jint ret;
    jstring jtask;
    jbyteArray jbuf;
    jbyte *jnbuf;

    getEnvStat = (*(self->vm))->GetEnv(self->vm,  (void **)&env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED) {
        if ((*(self->vm))->AttachCurrentThread(self->vm, &env, NULL) != 0) {
            return -1;
        }
    } else if (getEnvStat == JNI_OK) {
        //
    } else if (getEnvStat == JNI_EVERSION) {
        return -1;
    }

    jtask = (*env)->NewStringUTF(env, task);

    jbuf = (*env)->NewByteArray(env, len);

    ret = (*env)->CallIntMethod(env, self->adapter, self->callback, jtask, jbuf);

    if (ret > 0 && ret <= len) {
        jnbuf = (*env)->GetByteArrayElements(env, jbuf, NULL);
        memcpy(buf, jnbuf, (size_t) ret);
        (*env)->ReleaseByteArrayElements(env, jbuf, jnbuf, 0);
    }

    return ret;
}

int                    inputStreamAdapter_close(InputStreamAdapter_t *self)
{
    JNIEnv * env;
    callback(self, "close", NULL, 0);
    if ((*(self->vm))->GetEnv(self->vm,  (void **)&env, JNI_VERSION_1_6) == JNI_OK) {
        // Not sure why, but we must not detach the thread here.
        // (*(self->vm))->DetachCurrentThread(self->vm);
    }

    free(self);

    return 0;
}
ssize_t                 inputStreamAdapter_read(InputStreamAdapter_t *self, void * buf, size_t len)
{
    return callback(self, "read", buf, len);
}


static int __eof (void *userdata)
{
    InputStreamAdapter_t *self = userdata;

    return callback(self, "eof", NULL, 0);
}

coolmic_iohandle_t *    inputStreamAdapter_new_iohandle(JNIEnv * env, jobject adapter)
{
    InputStreamAdapter_t *self = inputStreamAdapter_new(env, adapter);
    if (!self)
        return NULL;

    return coolmic_iohandle_new(NULL, igloo_RO_NULL, self, inputStreamAdapter_close, inputStreamAdapter_read, __eof);
}
