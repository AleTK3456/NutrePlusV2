package alexander.nutreplusv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button tienda = findViewById(R.id.btntienda);
        Button imc = findViewById(R.id.btncalculadora);
        Button social = findViewById(R.id.btnredsocial);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);


        tienda.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Store.class);
            startActivity(intent);
        });

        imc.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Calculadora_IMC.class);
            intent.putExtra("username", getIntent().getStringExtra("username"));
            startActivity(intent);
        });

        social.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Socialplus.class);
            startActivity(intent);
        });

        btnCerrarSesion.setOnClickListener(view -> cerrarSesion());
    }

    private void cerrarSesion() {
        // Eliminar la sesión guardada
        SharedPreferences sharedPreferences = getSharedPreferences("SessionPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        // Volver a la pantalla de login
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}