# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.10.2)

option(BUILD_SHARED_LIBS "" ON)
add_subdirectory(libogg)