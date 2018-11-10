LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := coolmic-dsp
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -I$(LOCAL_PATH)/../opus -I$(LOCAL_PATH)/libcoolmic-dsp/include -fsigned-char -DHAVE_SNDDRV_DRIVER_OPENSL -DHAVE_SNDDRV_DRIVER_STDIO -DHAVE_ENC_OPUS -DHAVE_ENC_OPUS_BROKEN_INCLUDE_PATH
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp

LOCAL_SHARED_LIBRARIES := libogg libvorbis libshout libopus

#-lshout -L$(LOCAL_PATH)/../libshout/src/.libs/

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lOpenSLES

SOURCE_FILES = coolmic-dsp.c enc.c enc_vorbis.c enc_opus.c common_opus.c iohandle.c shout.c simple.c snddev.c vumeter.c tee.c snddev_null.c snddev_sine.c snddev_opensl.c snddev_stdio.c util.c metadata.c logging.c transform.c 

LOCAL_SRC_FILES := $(foreach c,$(SOURCE_FILES),libcoolmic-dsp/src/$c)

include $(BUILD_SHARED_LIBRARY)
