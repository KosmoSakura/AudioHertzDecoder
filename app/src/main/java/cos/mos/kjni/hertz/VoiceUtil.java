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
     * 获取声音的分贝
     *
     * @param bufferRead
     * @param lenght
     * @return
     */
    public static double getVolume(byte[] bufferRead, int lenght) {
        int volume = 0;

        for (int i = 0; i < bufferRead.length; i++) {
            volume += bufferRead[i] * bufferRead[i];
        }

        double mean = volume / (float) lenght;
        return mean;//10 * Math.log10(mean);
    }
}
