LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := coolmic-dsp
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -fsigned-char -DHAVE_SNDDRV_DRIVER_OPENSL
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp

LOCAL_SHARED_LIBRARIES := libogg libvorbis libshout

#-lshout -L$(LOCAL_PATH)/../libshout/src/.libs/

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lOpenSLES

LOCAL_SRC_FILES := \
	coolmic-dsp.c \
	enc.c \
	iohandle.c \
	shout.c \
	simple.c \
	snddev.c \
	snddev_null.c\

include $(BUILD_SHARED_LIBRARY)
