package cos.mos.kjni.util;

/**
 * @Description: 实验室
 * @Author: Kosmos
 * @Date: 2019.05.14 16:33
 * @Email: KosmoSakura@gmail.com
 */
public class Laboratory {
    static {
        System.loadLibrary("sakura");
    }

    public static void main(String[] args) {
        System.out.println(":->" + stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native String stringFromJNI();
}
