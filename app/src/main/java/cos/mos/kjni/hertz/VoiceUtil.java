package cos.mos.kjni.hertz;

/**
 * @Description: 声音计算工具类
 * @Author: shine
 * @Date: 18-10-15.
 * @Email: 13735542460@163.com
 * https://github.com/xuanxuandaoren/SoundAnalysis/tree/master
 */
public class VoiceUtil {
    /**
     * @param bufferRead 字节流
     * @param read     字节长度
     * @return 获取声音的分贝
     */
    public static double getVolume(byte[] bufferRead, int read) {
        int volume = 0;
        for (byte b : bufferRead) {
            volume += b * b;
        }
        //10 * Math.log10(mean);
        return volume / (float) read;
    }
}
