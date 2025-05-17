project("ncnn_runtime")
cmake_minimum_required(VERSION 3.22.1)


set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-2.4.13.7-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20240820-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(ncnn_runtime SHARED yolov8ncnn.cpp  paddleocr_ncnn.cpp common.cpp clipper.cpp)


target_link_libraries(ncnn_runtime ncnn ${OpenCV_LIBS} jnigraphics)