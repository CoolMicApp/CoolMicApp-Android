# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

add_library(coolmic-dsp-java SHARED InputStreamAdapter.c wrapper.c)

target_compile_options(coolmic-dsp-java PRIVATE -fsigned-char -std=c99)

target_include_directories(coolmic-dsp-java PUBLIC ${CMAKE_CURRENT_LIST_DIR})

find_library(log-lib log)

target_link_libraries(coolmic-dsp-java ${log-lib} coolmic-dsp)