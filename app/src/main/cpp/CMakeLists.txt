# 官方文档：https://developer.android.com/ndk/guides
# 所需cmake的最低版本
cmake_minimum_required(VERSION 3.4.1)

# 创建并命名库，将其设置为静态或共享，并提供其源代码的相对路径。
# 您可以定义多个库，CMake为你建造它们。
# Gradle会自动打包与您的APK共享的库。
# 3个参数：
#参数一：设置库的名称。
#参数二：将库设置为共享库。
#参数三：提供源文件的相对路径。
add_library(native-lib SHARED native-lib.cpp)

# 搜索指定的预建库并将路径存储为变量
# 因为CMake默认包含搜索路径中的系统库，只需指定要添加的公共NDK库的名称
# CMake在完成构建之前验证该库是否存在。
# 两个参数：
#参数一：设置路径变量的名称。
#参数二：指定要CMAKE定位的NDK库的名称。
find_library(log-lib log)

# 指定库cmake应链接到目标库。
# 可以链接多个库,例如，在此生成脚本中定义的库，预建的第三方库或系统库。
# 两个参数：
#参数一：指定目标库。
#参数二：将目标库链接到包含在NDK中的日志库。
target_link_libraries(native-lib ${log-lib})