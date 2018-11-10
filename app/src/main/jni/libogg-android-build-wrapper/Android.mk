LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libogg
LOCAL_C_INCLUDES = $(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/libogg/include $(LOCAL_PATH)
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -I$(LOCAL_PATH)/libogg/include -I$(LOCAL_PATH)/libogg/src -ffast-math -fsigned-char
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp


LOCAL_SRC_FILES := \
	libogg/src/bitwise.c \
	libogg/src/framing.c



include $(BUILD_SHARED_LIBRARY)
