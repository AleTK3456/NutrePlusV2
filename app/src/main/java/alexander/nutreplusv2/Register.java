package alexander.nutreplusv2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText etEmail, etUser, etPass;
    private Button btnCrearCuenta;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        // Configurar el botón de crear cuenta
        btnCrearCuenta.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            // Validar que los campos no estén vacíos
            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Register.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear objeto con los datos del usuario
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("username", username);
            userData.put("password", password);

            // Guardar en Firebase Database
            mDatabase.child("users").child(username).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Register.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                        finish(); // Volver a la pantalla anterior
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Register.this, "Error al registrar usuario: " + e.getMessage(), 
                                     Toast.LENGTH_SHORT).show();
                    });
        });
    }
}