cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_STANDARD 14)

include_directories(libs/include)

add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libavutil-56.so)

add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libswresample-3.so)

add_library(avcodec
        SHARED
        IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libavcodec-58.so)

add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libavfilter-7.so)

add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libswscale-5.so)

add_library(avdevice
        SHARED
        IMPORTED)
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libavdevice-58.so)

add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libavformat-58.so)

file(GLOB_RECURSE native_cpps "src/main/cpp/*.c")
file(GLOB_RECURSE native_cs "src/main/cpp/*/*.c")


add_library(
        orange
        SHARED
        src/main/jni/orange.cpp
        #        src/main/cpp/common/common_tools.h
        #        src/main/cpp/media/media_tools.c
        #        src/main/cpp/media/yuv_tools.c
        #        src/main/cpp/media/rgb_tools.c
        #        src/main/cpp/media/pcm_tools.c
        #        src/main/cpp/media/h264_tools.c

        #        src/main/cpp/gpuimage/GPUObject.cpp
        #        src/main/cpp/gpuimage/GLProgram.cpp
        #        src/main/cpp/gpuimage/GPUImageFramebuffer.cpp
        #        src/main/cpp/gpuimage/GPUImageFramebufferCache.cpp
        #        src/main/cpp/gpuimage/GPUImageUtil.cpp

        #        src/main/cpp/*.cpp
        ${native_cpps}
        ${native_cs}
)

find_library(
        log-lib
        log)

target_link_libraries(
        orange
        log
        avutil
        swresample
        avcodec
        avfilter
        swscale
        avdevice
        avformat)
