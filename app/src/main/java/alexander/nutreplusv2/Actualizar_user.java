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

public class Actualizar_user extends AppCompatActivity {

    private EditText etUser, etNewPassword;
    private Button btnActualizar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actualizar_user);

        // Inicializar Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference().child("users");

        // Inicializar vistas 
        etUser = findViewById(R.id.UpUser);
        etNewPassword = findViewById(R.id.UpPass);
        btnActualizar = findViewById(R.id.update);

        // Botón de actualización
        btnActualizar.setOnClickListener(view -> {
            String username = etUser.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            if (!username.isEmpty() && !newPassword.isEmpty()) {
                actualizarContraseña(username, newPassword);
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarContraseña(String username, String newPassword) {
        // Primero verificamos si el usuario existe
        mDatabase.child(username).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        // El usuario existe, actualizamos la contraseña
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("password", newPassword);

                        mDatabase.child(username).updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, 
                                    "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, 
                    "Error al buscar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}