LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := coolmic-dsp-java
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -I$(LOCAL_PATH)/../include/shout -I$(LOCAL_PATH)/../libcoolmic-dsp/libcoolmic-dsp/include/ -fsigned-char -std=c99
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp

LOCAL_SHARED_LIBRARIES := libogg libvorbis libshout libcoolmic-dsp

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
#-lshout -L$(LOCAL_PATH)/../libshout/src/.libs/

LOCAL_SRC_FILES := \
	wrapper.c

include $(BUILD_SHARED_LIBRARY)
