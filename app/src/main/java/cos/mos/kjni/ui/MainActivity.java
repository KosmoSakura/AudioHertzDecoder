package cos.mos.kjni.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import cos.mos.kjni.R;
import cos.mos.kjni.util.ULog;
import cos.mos.kjni.util.UTuner;
import cos.mos.kjni.util.UUnit;

/**
 * https://www.csdn.net/gather_22/MtTakg5sNTUyNy1ibG9n.html
 * https://blog.csdn.net/kobe269/article/details/40378377
 * https://github.com/houxiaohou/Guitar-Tunner
 * https://www.cnblogs.com/tt2015-sz/p/5616534.html
 */
public class MainActivity extends AppCompatActivity {
    private TextView frequencyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frequencyView = findViewById(R.id.sample_text);
        findViewById(R.id.bb1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ULog.commonD("点击开始");
                runnn();
            }
        });
        findViewById(R.id.bb2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ULog.commonD("点击结束");
                claer();
            }
        });
        checkPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void claer() {
        UTuner.instance().clear();
    }

    private void runnn() {
        UTuner.instance().run(new UTuner.HzListener() {
            @Override
            public void awaken(double hertz, double volume) {
                String hz = UUnit.unitsFormat(hertz, "Hz");
                ULog.commonD(hz);
                frequencyView.setText(hz);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        boolean st = true;
        for (String str : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, str)) {
                st = false;
                break;
            }
        }
        if (!st) {
            ActivityCompat.requestPermissions(this, permissions, 10086);
        }
    }
}
