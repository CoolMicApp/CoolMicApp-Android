cmake_minimum_required(VERSION 3.10.2)

project(CoolMic )

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_CURRENT_LIST_DIR}/cmake" )


add_library(commoninclude INTERFACE)
target_include_directories(commoninclude INTERFACE include)

add_library(commoninclude-shout INTERFACE)
target_include_directories(commoninclude-shout INTERFACE include/shout)


add_subdirectory(libogg-android-build-wrapper)
add_subdirectory(libvorbis-android-build-wrapper)
add_subdirectory(opus-android-build-wrapper)
add_subdirectory(libigloo-android-build-wrapper)
add_subdirectory(libshout-android-build-wrapper)
add_subdirectory(libcoolmic-dsp-android-build-wrapper)
add_subdirectory(libcoolmic-dsp-java)
