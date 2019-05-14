#include <jni.h>
#include <string>
//cos.mos.kjni.util.Laboratory
extern "C" JNIEXPORT jstring JNICALL
Java_cos_mos_kjni_util_Laboratory_stringFromJNI(
        JNIEnv *env,
        jclass type) {
    std::string hello = "来自C++的数据";
    return env->NewStringUTF(hello.c_str());
}
