LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libigloo
LOCAL_CFLAGS += -I$(LOCAL_PATH)/include -I$(LOCAL_PATH)/libigloo/include -I$(LOCAL_PATH)/libigloo/src -ffast-math -fsigned-char -DHAVE_CONFIG_H=1

LOCAL_SRC_FILES := libigloo/net/sock.c libigloo/net/resolver.c libigloo/thread/thread.c libigloo/httpp/httpp.c libigloo/httpp/encoding.c libigloo/avl/avl.c libigloo/log/log.c libigloo/timing/timing.c libigloo/src/buffer.c libigloo/src/libigloo.c libigloo/src/list.c libigloo/src/ro.c

LOCAL_EXPORT_C_INCLUDES := -I$(LOCAL_PATH)/libigloo/include

include $(BUILD_SHARED_LIBRARY)
