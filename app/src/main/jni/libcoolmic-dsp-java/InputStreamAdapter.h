//
// Created by phschafft on 2019/06/03.
//

#include <jni.h>
#include <coolmic-dsp/iohandle.h>
#include "../libogg-android-build-wrapper/ogg/config_types.h"

#ifndef COOLMIC_INPUTSTREAMADAPTER_H
#define COOLMIC_INPUTSTREAMADAPTER_H

typedef struct InputStreamAdapter InputStreamAdapter_t;

InputStreamAdapter_t *  inputStreamAdapter_new(JNIEnv * env, jobject adapter);
int                     inputStreamAdapter_close(InputStreamAdapter_t *self);
ssize_t                 inputStreamAdapter_read(InputStreamAdapter_t *self, void * buf, size_t len);

coolmic_iohandle_t *    inputStreamAdapter_new_iohandle(JNIEnv * env, jobject adapter);

#endif //COOLMIC_INPUTSTREAMADAPTER_H
