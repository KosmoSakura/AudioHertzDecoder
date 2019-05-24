package cos.mos.kjni.decode;

/**
 * @Description: 口哨校验
 * @Author: Kosmos
 * @Date: 2019.05.23 18:33
 * @Email: KosmoSakura@gmail.com
 * 基于谷狗工具
 * musicg：https://code.google.com/archive/p/musicg/
 */
public class UWhistle {
    // detection parameters
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;

    public void clear() {
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
    }

    public void start(OnSignalsDetectedListener listener) {
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setOnSignalsDetectedListener(listener);
        detectorThread.start();
    }
}
