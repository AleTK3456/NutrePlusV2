package alexander.nutreplusv2;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnCrear, btnActualizar;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("SessionPrefs", MODE_PRIVATE);

        // Verificar si hay una sesión activa
        if (sharedPreferences.contains("username")) {
            // Si hay sesión activa, ir directamente a MainActivity
            String username = sharedPreferences.getString("username", "");
            irAMainActivity(username);
            return;
        }

        // Inicializar Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        // Inicializar vistas
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCrear = findViewById(R.id.btnCrear);
        btnActualizar = findViewById(R.id.btnActualizar);

        // Configurar botón de login
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar credenciales en Firebase
            verificarCredenciales(username, password);
        });

        // Función de botón crear cuenta
        btnCrear.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        // Función para ir a actualizar un usuario
        btnActualizar.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Actualizar_user.class);
            startActivity(intent);
        });
    }

    private void verificarCredenciales(String username, String password) {
        mDatabase.child(username).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String storedPassword = dataSnapshot.child("password").getValue(String.class);
                        
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Guardar sesión de forma persistente
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.commit(); // Usando commit() en lugar de apply() para asegurar escritura inmediata

                            // Ir a MainActivity
                            irAMainActivity(username);
                        } else {
                            Toast.makeText(Login.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(Login.this, 
                    "Error al verificar credenciales: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void irAMainActivity(String username) {
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.putExtra("username", username);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}