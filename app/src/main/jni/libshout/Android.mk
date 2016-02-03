LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libshout
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -I$(LOCAL_PATH)/../include/shout -I$(LOCAL_PATH)/libshout/src -I$(LOCAL_PATH)/libshout/src/common -I$(LOCAL_PATH)/libshout/include -ffast-math -fsigned-char -DHAVE_CONFIG_H=1 -DDISABLE_PTHREAD_BLA=1
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp
LOCAL_SHARED_LIBRARIES := libvorbis libogg

LOCAL_SRC_FILES := libshout/src/common/net/sock.c libshout/src/common/net/resolver.c libshout/src/common/thread/thread.c libshout/src/common/httpp/httpp.c libshout/src/common/httpp/encoding.c libshout/src/common/avl/avl.c  libshout/src/common/log/log.c  libshout/src/common/timing/timing.c libshout/src/util.c libshout/src/queue.c libshout/src/proto_http.c libshout/src/proto_xaudiocast.c libshout/src/proto_icy.c libshout/src/proto_roaraudio.c libshout/src/format_ogg.c libshout/src/format_webm.c libshout/src/format_mp3.c libshout/src/codec_vorbis.c libshout/src/codec_opus.c libshout/src/shout.c 

include $(BUILD_SHARED_LIBRARY)
