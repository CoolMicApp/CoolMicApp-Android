# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

add_library(ogg SHARED ${CMAKE_CURRENT_LIST_DIR}/libogg/src/bitwise.c ${CMAKE_CURRENT_LIST_DIR}/libogg/src/framing.c)

target_compile_options(ogg PRIVATE -ffast-math -fsigned-char)

target_include_directories(ogg
        PUBLIC
            ${CMAKE_CURRENT_LIST_DIR}/libogg/include
            ${CMAKE_CURRENT_LIST_DIR}
)

target_link_libraries(ogg commoninclude)