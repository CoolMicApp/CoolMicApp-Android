# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

include(AddPrefix.cmake)

list(APPEND coolmicDSPSources coolmic-dsp.c enc.c enc_vorbis.c enc_opus.c common_opus.c iohandle.c shout.c simple.c snddev.c vumeter.c tee.c snddev_null.c snddev_sine.c snddev_opensl.c snddev_stdio.c util.c metadata.c logging.c transform.c simple-segment.c)

add_prefix(coolmicDSPSources ${CMAKE_CURRENT_LIST_DIR}/libcoolmic-dsp/src/ "${coolmicDSPSources}")

add_library(coolmic-dsp SHARED ${coolmicDSPSources})

target_compile_options(coolmic-dsp PRIVATE -fsigned-char -DHAVE_SNDDRV_DRIVER_OPENSL -DHAVE_SNDDRV_DRIVER_STDIO -DHAVE_ENC_OPUS -DHAVE_ENC_OPUS_BROKEN_INCLUDE_PATH)

target_include_directories(coolmic-dsp PUBLIC ${CMAKE_CURRENT_LIST_DIR}/libcoolmic-dsp/include)

find_library(log-lib log)
find_library(opensles-lib OpenSLES)

target_link_libraries(coolmic-dsp ${log-lib} ${opensles-lib} ogg vorbis vorbisenc shout opus igloo)