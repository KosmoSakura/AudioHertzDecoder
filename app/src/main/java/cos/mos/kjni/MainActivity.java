package cos.mos.kjni;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
/**
 * https://www.csdn.net/gather_22/MtTakg5sNTUyNy1ibG9n.html
 *https://blog.csdn.net/kobe269/article/details/40378377
 *https://github.com/houxiaohou/Guitar-Tunner
 *https://www.cnblogs.com/tt2015-sz/p/5616534.html
 * */
public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
