package id.rizal.skripsi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {
    Context context = SplashScreenActivity.this;
    private int DELAY = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        final Timer timer = new Timer();
        timer.schedule(new Splash(), DELAY);
    }

    class Splash extends TimerTask {
        @Override
        public void run() {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }
    }
}
