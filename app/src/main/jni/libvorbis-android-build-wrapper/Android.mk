LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libvorbis
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/libvorbis/include
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -I$(LOCAL_PATH)/libvorbis/include -I$(LOCAL_PATH)/libvorbis/lib -ffast-math -fsigned-char
#LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp
LOCAL_SHARED_LIBRARIES := libogg

LOCAL_SRC_FILES := \
	libvorbis/lib/mdct.c		\
	libvorbis/lib/smallft.c	    \
	libvorbis/lib/block.c		\
	libvorbis/lib/envelope.c	\
	libvorbis/lib/window.c	    \
	libvorbis/lib/lsp.c		    \
	libvorbis/lib/lpc.c		    \
	libvorbis/lib/analysis.c	\
	libvorbis/lib/synthesis.c	\
	libvorbis/lib/psy.c		    \
	libvorbis/lib/info.c		\
	libvorbis/lib/floor1.c	    \
	libvorbis/lib/floor0.c	    \
	libvorbis/lib/res0.c		\
	libvorbis/lib/mapping0.c	\
	libvorbis/lib/registry.c	\
	libvorbis/lib/codebook.c	\
	libvorbis/lib/sharedbook.c	\
	libvorbis/lib/lookup.c	    \
	libvorbis/lib/bitrate.c	    \
	libvorbis/lib/vorbisfile.c	\
	libvorbis/lib/vorbisenc.c

include $(BUILD_SHARED_LIBRARY)
