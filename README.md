## 0.Read Me
> Android的媒体处理一直是件让人头疼的事情。Java倒是也有不少媒体第三方的处理库（`mp3agic`,`musicg`等）。但是Java那一波三折的运行方式，导致在它在处理大量运算（图像，音频计算等）的时候力不从心。
> 为此，Java提供了`native`关键字，通过`jni`调用`C/C++`的函数库来充分使用CPU资源。
> 比如这里有一个需求：Android录音机录音，然后`实时`的转换成`mp3`保持在SD卡上
## 1.Cmake
> AS支持`cmake`之后，ndk的编译变得不再像`Android.mk`那么痛苦（不过调试还是...
> 这里准备用[Lame](http://lame.sourceforge.net/)库进行音频处理
#### 1.1.处理
lame库下载好后，解压找到`libmp3lame`目录，保留调`.c`、`.h`等文件，复制到项目`cpp`文件夹下，然后开始对头文件等做些处理。因为Android不支持。
- 1.`util.h`中`extern ieee754_float32_t fast_log2(ieee754_float32_t x); `=>`extern float fast_log2(float x);`
- 2.`id3tag.c`、`machine.h`中将`HAVE_STRCHR`和`HAVE_MEMCPY`的`ifdef`结构体删掉
- 3.`fft.c`中去除`vector/lame_intrin.h`的头文件引用
- 4.`set_get.h`中将`include <lame.h>`改为`include "lame.h"`
## 2.jni
编写jni接口
#### 2.1.C引用
函数名指向Java中的全路径名，用`下划线 _`分隔
```c
#include <cwchar>
#include "lame_util.h"
#include "lamemp3/lame.h"
#include <jni.h>
static lame_global_flags *glf = NULL;

void Java_cos_mos_recorder_decode_ULame_close(JNIEnv *env, jclass type){
    lame_close(glf);
    glf = NULL;
}

jint Java_cos_mos_recorder_decode_ULame_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
                                        jshortArray buffer_r_, jint samples, jbyteArray mp3buf_) {
    jshort *buffer_l = env->GetShortArrayElements(buffer_l_, NULL);
    jshort *buffer_r = env->GetShortArrayElements(buffer_r_, NULL);
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);
    int result =lame_encode_buffer(glf, buffer_l, buffer_r, samples, (u_char*)mp3buf, mp3buf_size);
    env->ReleaseShortArrayElements(buffer_l_, buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_r_, buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}

jint Java_cos_mos_recorder_decode_ULame_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_) {
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize  mp3buf_size = env->GetArrayLength(mp3buf_);
    int result = lame_encode_flush(glf, (u_char*)mp3buf, mp3buf_size);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}

void Java_cos_mos_recorder_decode_ULame_init(JNIEnv *env, jclass type, jint inSampleRate, jint outChannel,
                                             jint outSampleRate, jint outBitrate, jint quality) {
    if(glf != NULL){
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
```
#### 2.2.C头文件

```c
#include <jni.h>

extern "C"
{
void Java_cos_mos_recorder_decode_ULame_close(JNIEnv *env, jclass type);

jint Java_cos_mos_recorder_decode_ULame_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
                                               jshortArray buffer_r_, jint samples,
                                               jbyteArray mp3buf_);

jint Java_cos_mos_recorder_decode_ULame_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_);

void Java_cos_mos_recorder_decode_ULame_init(JNIEnv *env, jclass type, jint inSampleRate,
                                                    jint outChannel, jint outSampleRate,
                                                    jint outBitrate, jint quality);
}
```
#### 2.3.Java的方法映射
各个参数的描述我直接写在注释里，这里就不多说了。
注意包名，这里和`2.2`、`2.3`中的函数名对应
```java
package cos.mos.recorder.decode;

/**
 * @Description: Lame的jni映射库
 * @Author: Kosmos
 * @Date: 2019.05.24 23:36
 * @Email: KosmoSakura@gmail.com
 */
public class ULame {
    static {
        System.loadLibrary("lame");
    }

    /**
     * @param inSamplerate  采样率(Hz)
     * @param inChannel     流中的通道数
     * @param outSamplerate 输出采样率(Hz)
     * @param outBitrate    压缩比(KHz)
     * @param quality       mp3质量
     * @apiNote 初始化
     * 关于质量∈[0,9]
     * 0->最高质量，最慢
     * 9->最低质量，最快
     * 通常：
     * 2->接近最好的质量，不太慢
     * 5->质量好，速度快
     * 7->音质还凑活, 非常快
     */
    public native static void init(int inSamplerate, int inChannel, int outSamplerate,
                                   int outBitrate, int quality);

    /**
     * @param bufferLeft  左声道的PCM数据
     * @param bufferRight 右声道的PCM数据.
     * @param samples     每个采样通道的样本数
     * @param mp3buf      指定最终编码的MP3流=>数组长度=7200+(1.25 * bufferLeft.length)
     *                    "7200 + (1.25 * buffer_l.length)" length array.
     * @return mp3buf中输出的字节数。可以为0。
     * -1: mp3buf太小
     * -2: 内存分配异常
     * -3: lame初始化失败
     * -4: 音质解析异常
     * @apiNote 缓冲区编码为mp3
     */
    public native static int encode(short[] bufferLeft, short[] bufferRight, int samples, byte[] mp3buf);


    /**
     * @param mp3buf 结果编码的MP3流。您必须指定至少7200字节。
     * @return 输出到encode中mp3buf的字节数，可能为0
     * @apiNote flush掉lame的缓冲区
     * 关于刷流：
     * 0.可能会返回最后的几个mp3帧列数组
     * 1.将刷新lame的内部PCM编码缓冲区，不足数列用0补满最终帧
     * 2.encode中的mp3buf至少>7200字节（否则可能一列都不够用）
     * 3.(如果有)将id3v1标签写入比特流
     */
    public native static int flush(byte[] mp3buf);

    /**
     * 关闭Lame.
     */
    public native static void close();
}
```

## 3.CmakeList.txt
> 先看看目录结构，注意，`CmakeList.txt`和`gradle`中所有路径的都是写的相对路径。所有下面的内容因项目而异。
> 具体的参考[谷狗的官方文档](https://developer.android.com/ndk/guides)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190525182229938.png)
具体的我写在注释里
```c
# 编译本地库时，需要的cmake的最低版本
cmake_minimum_required(VERSION 3.4.1)
# 生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/clibs/${ANDROID_ABI})
#设置变量SRC_DIR为lamemp3的所在路径
set(SRC_DIR src/main/cpp/lamemp3)
#指定头文件所在，可以多次调用，指定多个路径
include_directories(src/main/cpp/lamemp3)
#设定一个目录
aux_source_directory(src/main/cpp/lamemp3 SRC_LIST)
#将前面目录下所有的文件都添加进去
add_library(lame SHARED src/main/cpp/lame_util.cpp ${SRC_LIST})
# 添加编译本地库时所需要的依赖库，cmake已经知道系统库的路径，所以这里只需要指定使用log库
find_library(clog log)
target_link_libraries(lame ${clog})
```
## 4.build.gradle
见注释
```c
...
android {
    .....
    defaultConfig {
      .....
        //开启Cmake工具
        externalNativeBuild {
            cmake {
                cppFlags ""
                //需要的so版本
                abiFilters 'armeabi-v7a', 'arm64-v8a'
            }
        }
       .....
    }
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
    sourceSets {
        main {
            jniLibs.srcDirs = ['clibs']
        }
    }
   ....
}
....
```

## 5.录音MP3Recorder
录音机的相关操作，还是见注释。
>这里假装科普下：
> - 1.Android的`AudioRecord`录音的默认格式为：`PCM编码`，`16Bit`、`单声道`。
> - 2.我们平时说的音量其实是一个相对值，
> - - 2.1.计算方式：`volume=X*log10(P1/P2)`
> - - 2.2.`P1`:测量值的声压、`P2`:参考值的声压、`X`为参考系数
> - - 2.3.人类所能听到的最小声压:`20微帕`
> - 3.人类能听到的声音频率`∈[20Hz，20KHz]`
> - - 3.1.下面的频率计算方式为：[FFT(快速傅里叶变换)](https://baike.baidu.com/item/%E5%BF%AB%E9%80%9F%E5%82%85%E9%87%8C%E5%8F%B6%E5%8F%98%E6%8D%A2/214957?fr=aladdin)
> - 4.`音频帧`计算公式： int size = 采样率 * 位宽 * 采样时间 * 通道数
> - 5.缓冲区：大小不能低于一音频帧的大小（所以下面代码有个四舍五入到音频帧大小的步骤）
> - 6.采样率：每秒钟能够采样的次数

```java
package cos.mos.recorder.decode;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * @Description: 录音工具
 * @Author: Kosmos
 * @Date: 2019.05.25 15:16
 * @Email: KosmoSakura@gmail.com
 */
public class MP3Recorder {
    //Recorder
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;//音源：麦克风
    private static final int DEFAULT_SAMPLING_RATE = 44100;//采样率：模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;//单声道
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;///PCM编码,16Bit
    //Lame
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;//音质
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;//mono=>1
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;//编码比特率
    //采样配置
    private static final int FRAME_COUNT = 160;//每160帧作为一个数列周期，通知编码
    private AudioRecord record;
    private int bufferSize;
    private short[] bufferPCM;
    private DataEncodeThread encodeThread;
    private boolean isRecording = false;

    /**
     * @apiNote 采样率1通道，16位pcm
     */
    public MP3Recorder() {
    }

    public void start(File targetFile) throws IOException {
        if (isRecording) {
            return;
        }
        isRecording = true; //提早，防止init或startRecording被多次调用
        initAudioRecorder(targetFile);
        record.startRecording();
        new Thread() {
            @Override
            public void run() {
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (isRecording) {
                    int readSize = record.read(bufferPCM, 0, bufferSize);
                    if (readSize > 0) {
                        encodeThread.addTask(bufferPCM, readSize);
                        calculateRealVolume(bufferPCM, readSize);
                    }
                }
                //释放
                record.stop();
                record.release();
                record = null;
                // 等待完成转码
                encodeThread.sendStopMessage();
            }

            /**
             * @apiNote 此计算方法来自samsung开发范例
             * @param buffer buffer
             * @param readSize readSize
             */
            private void calculateRealVolume(short[] buffer, int readSize) {
                double sum = 0;
                for (int i = 0; i < readSize; i++) {
                    // 这里没有做运算的优化，为了更加清晰的展示代码
                    sum += buffer[i] * buffer[i];
                }
                if (readSize > 0) {
                    double amplitude = sum / readSize;
                    mVolume = (int) Math.sqrt(amplitude);
                }
            }
        }.start();
    }

    private int mVolume;

    /**
     * @return 真实音量
     * @apiNote 获取真实的音量。 [算法来自三星]
     */
    public int getRealVolume() {
        return mVolume;
    }


    public void stop() {
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    /**
     * @param targetFile 目标文件
     * @apiNote 初始化
     */
    private void initAudioRecorder(File targetFile) throws IOException {
        bufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
            DEFAULT_AUDIO_FORMAT.getAudioFormat());
        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();//得到样本个数。计算缓冲区大小
        //四舍五入到给定帧大小，方便整除，以通知
        int frameSize = bufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            bufferSize = frameSize * bytesPerFrame;
        }
        record = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE,
            DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(), bufferSize);
        bufferPCM = new short[bufferSize];
        //初始化lame缓冲区mp3采样速率与所记录的pcm采样速率相同，比特率为32kbps
        ULame.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE,
            DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
        encodeThread = new DataEncodeThread(targetFile, bufferSize);
        encodeThread.start();
        record.setRecordPositionUpdateListener(encodeThread, encodeThread.getHandler());
        record.setPositionNotificationPeriod(FRAME_COUNT);
    }
}
```
## 6.中转
- 1.过程耗时，建议交给子线程做
- 2.从缓冲区读取数据，使用`lame`编码MP3
- 3.缓冲区不足时，使用lame将所有数据刷新到文件中，并添加`MP3尾信息`

```java
	/**
     * @return 从缓冲区中读取的数据的长度（没有数据时返回0）
     */
    private int processData() {
        if (mTasks.size() > 0) {
            Task task = mTasks.remove(0);
            short[] buffer = task.getData();
            int readSize = task.getReadSize();
            int encodedSize = ULame.encode(buffer, buffer, readSize, mp3Buffer);
            if (encodedSize > 0) {
                try {
                    outputStream.write(mp3Buffer, 0, encodedSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return readSize;
        }
        return 0;
    }
```

```java
   private void flushAndRelease() {
        //将MP3尾信息写入buffer中
        final int flushResult = ULame.flush(mp3Buffer);
        if (flushResult > 0) {
            try {
                outputStream.write(mp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ULame.close();
            }
        }
    }
```
---

## License

```
   Copyright 2019 Kosmos

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```