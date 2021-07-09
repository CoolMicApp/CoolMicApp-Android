# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

add_library(vorbis SHARED 
    libvorbis/lib/mdct.c
    libvorbis/lib/smallft.c
    libvorbis/lib/block.c
    libvorbis/lib/envelope.c
    libvorbis/lib/window.c
    libvorbis/lib/lsp.c
    libvorbis/lib/lpc.c
    libvorbis/lib/analysis.c
    libvorbis/lib/synthesis.c
    libvorbis/lib/psy.c
    libvorbis/lib/info.c
    libvorbis/lib/floor1.c
    libvorbis/lib/floor0.c
    libvorbis/lib/res0.c
    libvorbis/lib/mapping0.c
    libvorbis/lib/registry.c
    libvorbis/lib/codebook.c
    libvorbis/lib/sharedbook.c
    libvorbis/lib/lookup.c
    libvorbis/lib/bitrate.c
    libvorbis/lib/vorbisfile.c
    libvorbis/lib/vorbisenc.c
)

target_compile_options(vorbis PRIVATE -ffast-math -fsigned-char)

target_include_directories(vorbis
        PUBLIC
            ${CMAKE_CURRENT_LIST_DIR}/libvorbis/include
        PRIVATE
            ${CMAKE_CURRENT_LIST_DIR}/libvorbis/lib
)

target_link_libraries(vorbis commoninclude ogg)