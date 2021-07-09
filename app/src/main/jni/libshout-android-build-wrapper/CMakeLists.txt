# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

add_library(shout
        SHARED
            libshout/src/common/net/sock.c
            libshout/src/common/net/resolver.c
            libshout/src/common/thread/thread.c
            libshout/src/common/httpp/httpp.c
            libshout/src/common/httpp/encoding.c
            libshout/src/common/avl/avl.c
            libshout/src/common/log/log.c
            libshout/src/common/timing/timing.c
            libshout/src/util.c
            libshout/src/queue.c
            libshout/src/proto_http.c
            libshout/src/proto_xaudiocast.c
            libshout/src/proto_icy.c
            libshout/src/proto_roaraudio.c
            libshout/src/format_ogg.c
            libshout/src/format_webm.c
            libshout/src/format_mp3.c
            libshout/src/codec_vorbis.c
            libshout/src/codec_opus.c
            libshout/src/shout.c
            libshout/src/connection.c
)

target_compile_options(shout PRIVATE -ffast-math -fsigned-char -DHAVE_CONFIG_H=1 -DDISABLE_PTHREAD_BLA=1)

target_include_directories(shout
        PRIVATE
            ${CMAKE_CURRENT_LIST_DIR}/libshout/src
            ${CMAKE_CURRENT_LIST_DIR}/libshout/src/common
            ${CMAKE_CURRENT_LIST_DIR}/libshout/include
)

target_link_libraries(shout commoninclude commoninclude-shout ogg vorbis)