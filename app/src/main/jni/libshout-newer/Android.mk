LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libshout
LOCAL_SRC_FILES := libshout.so
include $(PREBUILT_SHARED_LIBRARY)
