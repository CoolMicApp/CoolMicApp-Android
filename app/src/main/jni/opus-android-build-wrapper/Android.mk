LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include $(LOCAL_PATH)/opus/celt_sources.mk
include $(LOCAL_PATH)/opus/silk_sources.mk
include $(LOCAL_PATH)/opus/opus_sources.mk

LOCAL_MODULE := opus
LOCAL_CFLAGS += -I$(LOCAL_PATH)/opus/include -I$(LOCAL_PATH)/ -I$(LOCAL_PATH)/opus/celt/ -I$(LOCAL_PATH)/opus/silk/ -I$(LOCAL_PATH)/opus/silk/float/  -fsigned-char -DHAVE_CONFIG_H=1
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp

LOCAL_SRC_FILES := $(addprefix opus/, $(CELT_SOURCES)) $(addprefix opus/, $(SILK_SOURCES) $(SILK_SOURCES_FLOAT)) $(addprefix opus/, $(OPUS_SOURCES) $(OPUS_SOURCES_FLOAT))

include $(BUILD_SHARED_LIBRARY)
