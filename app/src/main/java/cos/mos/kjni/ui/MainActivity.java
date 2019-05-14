package cos.mos.kjni.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import cos.mos.kjni.R;
import cos.mos.kjni.util.Laboratory;

/**
 * https://www.csdn.net/gather_22/MtTakg5sNTUyNy1ibG9n.html
 * https://blog.csdn.net/kobe269/article/details/40378377
 * https://github.com/houxiaohou/Guitar-Tunner
 * https://www.cnblogs.com/tt2015-sz/p/5616534.html
 */
public class MainActivity extends AppCompatActivity {
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.sample_text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(Laboratory.stringFromJNI());
            }
        });
    }


}
