package cos.mos.kjni.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cos.mos.kjni.R;
import cos.mos.kjni.util.TunnerThread;
import cos.mos.kjni.util.UUnit;

/**
 * https://www.csdn.net/gather_22/MtTakg5sNTUyNy1ibG9n.html
 * https://blog.csdn.net/kobe269/article/details/40378377
 * https://github.com/houxiaohou/Guitar-Tunner
 * https://www.cnblogs.com/tt2015-sz/p/5616534.html
 */
public class MainActivity extends AppCompatActivity {
    private boolean startRecording = true;
    private TunnerThread tunner;
    private Button tunning_button;
    private TextView frequencyView;
    private Handler handler = new Handler();
    private Runnable callback = new Runnable() {
        public void run() {
            updateText(tunner.getCurrentFrequency());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frequencyView = findViewById(R.id.sample_text);
        tunning_button = findViewById(R.id.tunning_button);
        tunning_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                frequencyView.setText(Laboratory.stringFromJNI());
                onRecord(startRecording);
                if (startRecording) {
                    tunning_button.setText("停止");
                } else {
                    tunning_button.setText("启动");
                }
                startRecording = !startRecording;
            }
        });
        checkPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

    private void onRecord(boolean startRecording) {
        if (startRecording) {
            startTunning();
        } else {
            stopTunning();
        }
    }

    private void startTunning() {
        tunner = new TunnerThread(handler, callback);
        tunner.start();
    }

    private void stopTunning() {
        tunner.close();
    }

    private void updateText(double currentFrequency) {
//        while (currentFrequency < 82.41) {
//            currentFrequency = currentFrequency * 2;
//        }
//        while (currentFrequency > 164.81) {
//            currentFrequency = currentFrequency * 0.5;
//        }
//        BigDecimal a = new BigDecimal(currentFrequency);
//        BigDecimal result = a.setScale(2, RoundingMode.DOWN);
        frequencyView.setText(UUnit.unitsFormat(currentFrequency, "Hz"));
    }

}
