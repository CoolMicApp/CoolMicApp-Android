# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

include(ReadVariables.cmake)
include(AddPrefix.cmake)

ReadVariables(opus/celt_sources.mk)
ReadVariables(opus/silk_sources.mk)
ReadVariables(opus/opus_sources.mk)

list(APPEND libOPUSSources ${CELT_SOURCES} ${SILK_SOURCES} ${SILK_SOURCES_FLOAT} ${OPUS_SOURCES} ${OPUS_SOURCES_FLOAT})

add_prefix(libOPUSSources ${CMAKE_CURRENT_LIST_DIR}/opus/ "${libOPUSSources}")

add_library(opus SHARED ${libOPUSSources})

target_compile_options(opus PRIVATE -fsigned-char -DHAVE_CONFIG_H=1)

target_include_directories(opus
        PUBLIC
            ${CMAKE_CURRENT_LIST_DIR}
        PRIVATE
            ${CMAKE_CURRENT_LIST_DIR}/opus/include
            ${CMAKE_CURRENT_LIST_DIR}/opus/celt
            ${CMAKE_CURRENT_LIST_DIR}/opus/silk
            ${CMAKE_CURRENT_LIST_DIR}/opus/silk/float
            ${CMAKE_CURRENT_LIST_DIR}
)

target_link_libraries(opus commoninclude)