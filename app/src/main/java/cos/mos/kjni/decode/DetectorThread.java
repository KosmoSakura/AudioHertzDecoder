package cos.mos.kjni.decode;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.musicg.wave.WaveHeader;

import java.util.LinkedList;


public class DetectorThread extends Thread {
    private RecorderThread recorder;
    private WhistleCheck whistleApi;
    private volatile Thread _thread;

    private LinkedList<Boolean> whistleResultList = new LinkedList<Boolean>();
    private int numWhistles;
    private int whistleCheckLength = 3;
    private int whistlePassScore = 3;

    private OnSignalsDetectedListener onSignalsDetectedListener;

    public DetectorThread(RecorderThread recorder) {
        this.recorder = recorder;
        AudioRecord audioRecord = recorder.getAudioRecord();

        int bitsPerSample = 0;
        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bitsPerSample = 16;
        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
            bitsPerSample = 8;
        }

        int channel = 0;
        // whistle detection only supports mono channel
        if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
            channel = 1;
        }

        WaveHeader waveHeader = new WaveHeader();
        waveHeader.setChannels(channel);
        waveHeader.setBitsPerSample(bitsPerSample);
        waveHeader.setSampleRate(audioRecord.getSampleRate());
        whistleApi = new WhistleCheck(waveHeader);
    }

    private void initBuffer() {
        numWhistles = 0;
        whistleResultList.clear();
        // 初始化第一个帧
        for (int i = 0; i < whistleCheckLength; i++) {
            whistleResultList.add(false);
        }
    }

    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void stopDetection() {
        _thread = null;
    }

    public void run() {
        try {
            byte[] buffer;
            initBuffer();
            Thread thisThread = Thread.currentThread();
            while (_thread == thisThread) {
                // 检测到声音
                buffer = recorder.getFrameBytes();
                // 音频分析
                if (buffer != null) {
                    // 声音检测:吹口哨
                    boolean isWhistle = whistleApi.isWhistle(buffer);
                    if (whistleResultList.getFirst()) {
                        numWhistles--;
                    }
                    whistleResultList.removeFirst();
                    whistleResultList.add(isWhistle);
                    if (isWhistle) {
                        numWhistles++;
                    }
                    if (numWhistles >= whistlePassScore) {
                        // 清除缓存
                        initBuffer();
                        onWhistleDetected();
//                        ULog.commonE("口哨：" + isWhistle, "Kosmos");
                    } else {
//                        ULog.commonD("口哨：" + isWhistle, "Kosmos");
                    }
                } else {
                    // 发现没有声音
                    if (whistleResultList.getFirst()) {
                        numWhistles--;
                    }
                    whistleResultList.removeFirst();
                    whistleResultList.add(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onWhistleDetected() {

        if (onSignalsDetectedListener != null) {
            onSignalsDetectedListener.onWhistleDetected();
        }
    }

    public void setOnSignalsDetectedListener(OnSignalsDetectedListener listener) {
        onSignalsDetectedListener = listener;
    }
}