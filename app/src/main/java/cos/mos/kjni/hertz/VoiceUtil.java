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
     * @param read       字节长度
     * @return 获取声音的分贝
     * @apiNote 关于音量（这是一个相对单位）
     * 20微帕，人类所能听到的最小声压
     * volume=20*log10(P1/P2)
     * P1:测量值的声压
     * P2:参考值的声压
     * 参考文档：
     * https://blog.csdn.net/greatpresident/article/details/38402147
     * https://blog.csdn.net/lhmin5200/article/details/65632915
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
