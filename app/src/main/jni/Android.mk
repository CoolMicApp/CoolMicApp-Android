LOCAL_PATH := $(call my-dir)

LOCAL_C_INCLUDE := $(LOCAL_PATH)/include

include $(addprefix $(LOCAL_PATH)/, $(addsuffix /Android.mk, \
    opus-android-build-wrapper \
    libogg-android-build-wrapper \
	libvorbis-android-build-wrapper \
    libshout-android-build-wrapper \
    libcoolmic-dsp-android-build-wrapper \
    libcoolmic-dsp-java \
))


