package alexander.nutreplusv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashscreen);

        progressBar = findViewById(R.id.progressBar);

        // Verificar sesión antes de iniciar la animación
        SharedPreferences sharedPreferences = getSharedPreferences("SessionPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                handler.post(() -> progressBar.setProgress(progressStatus));
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            handler.post(() -> {
                Intent intent;
                if (username != null) {
                    // Si hay sesión activa, ir a MainActivity
                    intent = new Intent(Splashscreen.this, MainActivity.class);
                    intent.putExtra("username", username);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    // Si no hay sesión, ir a Login
                    intent = new Intent(Splashscreen.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                startActivity(intent);
                finish();
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el handler para optimizar app
        handler.removeCallbacksAndMessages(null);
    }
}