LOCAL_PATH := $(call my-dir)

LOCAL_C_INCLUDE := $(LOCAL_PATH)/include

include $(addprefix $(LOCAL_PATH)/, $(addsuffix /Android.mk, \
    opus \
    libshout \
    libogg \
	libvorbis \
    libcoolmic-dsp \
    libcoolmic-dsp-java \
))


