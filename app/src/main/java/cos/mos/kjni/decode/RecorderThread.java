package cos.mos.kjni.decode;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import cos.mos.kjni.util.ULog;


public class RecorderThread extends Thread {
    private AudioRecord audioRecord;
    private boolean isRecording;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;//CHANNEL_CONFIGURATION_MONO
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 44100;//44100
    private int frameByteSize = 4096; // for 1024 fft size (16bit sample size)//4096
    byte[] buffer;

    public RecorderThread() {
        int recBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding); // need to be larger than size of a frame
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfiguration, audioEncoding, recBufSize);
        buffer = new byte[frameByteSize];
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public boolean isRecording() {
        return this.isAlive() && isRecording;
    }

    public void startRecording() {
        try {
            audioRecord.startRecording();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            audioRecord.stop();
            isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    public byte[] getFrameBytes() {
//        audioRecord.read(buffer, 0, frameByteSize);
//        // analyze sound
//        int totalAbsValue = 0;
//        int sample = frameByteSize;
//        int i;
//        for (i = 0; i < frameByteSize; i += 2) {
//            sample = buffer[i];
//            totalAbsValue += Math.abs((short) (buffer[(i + 1)] << 8 | sample));
//        }
//        if (totalAbsValue / sample / 2 < 30.0F) {
//            return null;
//        }
//        return buffer;
//    }

    public byte[] getFrameBytes() {
        audioRecord.read(buffer, 0, frameByteSize);
        // analyze sound
        int totalAbsValue = 0;
        float averageAbsValue = 0.0f;
        for (int i = 0; i < frameByteSize; i += 2) {
            totalAbsValue += Math.abs((short) ((buffer[i]) | buffer[i + 1] << 8));
        }
        averageAbsValue = totalAbsValue / frameByteSize / 2;
        ULog.commonD("计算值=" + averageAbsValue + "|||中频=" + totalAbsValue + "|||位宽=" + frameByteSize);
        if (averageAbsValue < 30) {
            return null;
        }
        return buffer;
    }

    public void run() {
        startRecording();
    }
}