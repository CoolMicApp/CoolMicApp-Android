# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

add_library(igloo SHARED libigloo/net/sock.c libigloo/net/resolver.c libigloo/thread/thread.c libigloo/httpp/httpp.c libigloo/httpp/encoding.c libigloo/avl/avl.c libigloo/log/log.c libigloo/timing/timing.c libigloo/src/buffer.c libigloo/src/libigloo.c libigloo/src/list.c libigloo/src/ro.c)

target_compile_options(igloo PRIVATE -ffast-math -fsigned-char -DHAVE_CONFIG_H=1)

target_include_directories(igloo
        PUBLIC
            ${CMAKE_CURRENT_LIST_DIR}/libigloo/include
            ${CMAKE_CURRENT_LIST_DIR}/include
        PRIVATE
            ${CMAKE_CURRENT_LIST_DIR}/libigloo/src
)


