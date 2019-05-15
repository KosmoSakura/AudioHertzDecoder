package cos.mos.kjni.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @Description: 解析音频流频率
 * @Author: Kosmos
 * @Date: 2019.05.15 10:10
 * @Email: KosmoSakura@gmail.com
 * 需要动态申请：<uses-permission android:name="android.permission.RECORD_AUDIO"/>
 */
public class UTuner {
    private static final int[] OPT_SAMPLE_RATES = {11025, 8000, 22050, 44100};
    private static final int[] BUFFERSIZE_PER_SAMPLE_RATE = {8 * 1024, 4 * 1024, 16 * 1024, 32 * 1024};
    private int SAMPLE_RATE = 8000;//采样率
    //缓冲区大小不能低于一音频帧的大小
    //音频帧计算公式： int size = 采样率 * 位宽 * 采样时间 * 通道数
    private int READ_BUFFERSIZE = 4 * 1024;//缓冲区大小
    private AudioRecord audioRecord;
    private Disposable subscribe;
    private static UTuner tuner;
    private boolean running;
    private final Handler delivery = new Handler(Looper.getMainLooper());

    private UTuner() {

    }

    public static UTuner instance() {
        if (tuner == null) {
            tuner = new UTuner();
        }
        return tuner;
    }

    private void check() {
        running = true;
        if (audioRecord == null) {
            // 每个device的初始化参数可能不同
            int counter = 0;
            for (int sampleRate : OPT_SAMPLE_RATES) {
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,//音频源：麦克风
                    sampleRate,//采样率：每秒钟能够采样的次数
                    AudioFormat.CHANNEL_IN_MONO,//声道：输入mono
                    AudioFormat.ENCODING_PCM_16BIT,//编码制式和采样大小：PCM编码,16Bit
                    sampleRate * 6);//缓冲区大小:采样率6倍
                //构建成功
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    SAMPLE_RATE = sampleRate;
                    READ_BUFFERSIZE = BUFFERSIZE_PER_SAMPLE_RATE[counter];
                    break;
                }
                counter++;
            }
        }
    }

    public void run(final HzListener listener) {
        check();
        audioRecord.startRecording();//开始采集
        final byte[] bufferRead = new byte[READ_BUFFERSIZE];
        final Runnable runnable = () -> listener.sampleRate(UJni.processSampleData(bufferRead, SAMPLE_RATE));
        subscribe = Observable.interval(50, TimeUnit.MICROSECONDS)
            .takeWhile(aLong -> running)
            .subscribe(aLong -> {
                if (audioRecord.read(bufferRead, 0, READ_BUFFERSIZE) >= AudioRecord.SUCCESS) {
                    delivery.post(runnable);
                }
            });
    }

    public void clear() {
        running = false;
        if (subscribe != null) {
            if (!subscribe.isDisposed()) {
                subscribe.dispose();
            }
        }
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
    }

    public interface HzListener {
        void sampleRate(double frequency);
    }
}
