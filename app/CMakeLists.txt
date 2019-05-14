# 官方文档：https://developer.android.com/ndk/guides
# 编译本地库时，需要的cmake的最低版本
cmake_minimum_required(VERSION 3.4.1)
# 生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/clibs/${ANDROID_ABI})
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})

# Gradle会自动打包与APK共享的库。
# add_library的3个参数：
# 参数1：编译生成本地库的名字
# 参数2：SHARED 表示生成的是动态链接库
# 参数3：(可变参数)参与编译文件的路径（注意，CMakeLists的路径，本页所有路径都是相对于CMakeLists而言
file(GLOB SOURCES src/main/cpp/*.cpp)
message(STATUS "SOURCES = ${SOURCES}")
add_library(sakura SHARED ${SOURCES})

# 添加编译本地库时所需要的依赖库，由于cmake已经知道系统库的路径，
# 所以这里只需要指定使用log库
# 2个参数：
# 参数1：log库的命名。
# 参数2：cmake定位的NDK库的名称（这里时'log'库）
find_library(clog log)

# 关联我们自己的库和一些第三方库或者系统库
# 2个参数：
# 参数1：指定目标库。
# 参数2：将目标库链接到包含在NDK中的日志库。
target_link_libraries(sakura ${clog})