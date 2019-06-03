#include <cwchar>
#include "lame_util.h"
#include "lamemp3/lame.h"
#include <jni.h>

#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>

static lame_global_flags *glf = NULL;

void Java_cos_mos_recorder_decode_ULame_close(JNIEnv *env, jclass type) {
    lame_close(glf);
    glf = NULL;
}

jint Java_cos_mos_recorder_decode_ULame_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
                                               jshortArray buffer_r_, jint samples,
                                               jbyteArray mp3buf_) {
    jshort *buffer_l = env->GetShortArrayElements(buffer_l_, NULL);
    jshort *buffer_r = env->GetShortArrayElements(buffer_r_, NULL);
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);
    int result = lame_encode_buffer(glf, buffer_l, buffer_r, samples, (u_char *) mp3buf,
                                    mp3buf_size);
    env->ReleaseShortArrayElements(buffer_l_, buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_r_, buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}

jint Java_cos_mos_recorder_decode_ULame_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_) {
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);
    int result = lame_encode_flush(glf, (u_char *) mp3buf, mp3buf_size);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}

void Java_cos_mos_recorder_decode_ULame_init(JNIEnv *env, jclass type, jint inSampleRate,
                                             jint outChannel,
                                             jint outSampleRate, jint outBitrate, jint quality) {
    if (glf != NULL) {
        lame_close(glf);
        glf = NULL;
    }
    glf = lame_init();
    lame_set_in_samplerate(glf, inSampleRate);
    lame_set_num_channels(glf, outChannel);
    lame_set_out_samplerate(glf, outSampleRate);
    lame_set_brate(glf, outBitrate);
    lame_set_quality(glf, quality);
    lame_init_params(glf);
}


///**
// * 返回值 char* 这个代表char数组的首地址
// *  Jstring2CStr 把java中的jstring的类型转化成一个c语言中的char 字符串
// */
//char *Jstring2CStr(JNIEnv *env, jstring jstr) {
//    char *rtn = NULL;
//    jclass clsstring = (*env)->FindClass(env, "java/lang/String"); //String
//    jstring strencode = (*env)->NewStringUTF(env, "GB2312"); // 得到一个java字符串 "GB2312"
//    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes",
//                                        "(Ljava/lang/String;)[B"); //[ String.getBytes("gb2312");
//    jbyteArray barr = (jbyteArray) (*env)->CallObjectMethod(env, jstr, mid,
//                                                            strencode); // String .getByte("GB2312");
//    jsize alen = (*env)->GetArrayLength(env, barr); // byte数组的长度
//    jbyte *ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
//    if (alen > 0) {
//        rtn = (char *) malloc(alen + 1); //"\0"
//        memcpy(rtn, ba, alen);
//        rtn[alen] = 0;
//    }
//    (*env)->ReleaseByteArrayElements(env, barr, ba, 0); //
//    return rtn;
//}
//
//int flag = 0;
///**
// * wav转换mp3
// */
//JNIEXPORT void
//JNICALLJava_cos_mos_recorder_decode_ULame_wav2Mp3(JNIEnv *env, jobject obj, jstring jwav,
//                                                  jstring jmp3,
//                                                  jint inSamplerate) {
//    //1.初始化lame的编码器
//    lame_t lameConvert = lame_init();
//    int channel = 1;//单声道
//    //2. 设置lame mp3编码的采样率
//    lame_set_in_samplerate(lameConvert, inSamplerate);
//    lame_set_out_samplerate(lameConvert, inSamplerate);
//    lame_set_num_channels(lameConvert, 1);
//    // 3. 设置MP3的编码方式
//    lame_set_VBR(lameConvert, vbr_default);
//    lame_init_params(lameConvert);
//    char *cwav = Jstring2CStr(env, jwav);
//    char *cmp3 = Jstring2CStr(env, jmp3);
//    const int SIZE = (inSamplerate / 20) + 7200;
//    //4.打开 wav,MP3文件
//    FILE *fwav = fopen(cwav, "rb");
//    fseek(fwav, 4 * 1024, SEEK_CUR);
//    FILE *fmp3 = fopen(cmp3, "wb+");
//    short int wav_buffer[SIZE * channel];
//    unsigned char mp3_buffer[SIZE];
//    int read;
//    int write; //代表读了多少个次 和写了多少次
//    int total = 0; // 当前读的wav文件的byte数目
//    do {
//        if (flag == 404) {
//            return;
//        }
//        read = fread(wav_buffer, sizeof(short int) * channel, SIZE, fwav);
//        total += read * sizeof(short int) * channel;
//        if (read != 0) {
//            write = lame_encode_buffer(lameConvert, wav_buffer, NULL, read, mp3_buffer, SIZE);
//            //write = lame_encode_buffer_interleaved(lame,wav_buffer,read,mp3_buffer,SIZE);
//        } else {
//            write = lame_encode_flush(lameConvert, mp3_buffer, SIZE);
//        }
//        //把转化后的mp3数据写到文件里
//        fwrite(mp3_buffer, sizeof(unsigned char), write, fmp3);
//
//    } while (read != 0);
//    lame_mp3_tags_fid(lameConvert, fmp3);
//    lame_close(lameConvert);
//    fclose(fwav);
//    fclose(fmp3);
//}