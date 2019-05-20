package cos.mos.kjni.util;

/**
 * @Description:
 * @Author: Kosmos
 * @Date: 2019.05.15 10:36
 * @Email: KosmoSakura@gmail.com
 */
public class UJni {
    static {
        System.loadLibrary("sakura");
    }

    public static native double runFFT(byte[] sample, int sampleRate);
}
