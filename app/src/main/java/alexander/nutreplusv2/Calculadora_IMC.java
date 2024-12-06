package alexander.nutreplusv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Calculadora_IMC extends AppCompatActivity {

    private CheckBox checkBoxHombre, checkBoxMujer;
    private EditText etPeso, etAltura;
    private Button btnCalcular, btnHistorial;
    private TextView tvResultadoActual, tvInterpretacion, tvHistorialResultados;
    private DatabaseReference mDatabase;
    private String currentUser; // Usuario actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculadora_imc);

        // Obtener el usuario actual del Intent
        currentUser = getIntent().getStringExtra("username");
        
        // Inicializar Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("imc_records");

        // Inicializar vistas
        initializeViews();
        
        // Configurar los CheckBox para selección única
        setupCheckBoxes();

        // Configurar el botón de calcular
        btnCalcular.setOnClickListener(v -> calcularIMC());

        // Configurar el botón de historial
        btnHistorial.setOnClickListener(v -> cargarHistorial());

        // Cargar historial inicial
        if (currentUser != null) {
            cargarHistorial();
        }
    }

    private void initializeViews() {
        checkBoxHombre = findViewById(R.id.checkBoxHombre);
        checkBoxMujer = findViewById(R.id.checkBoxMujer);
        etPeso = findViewById(R.id.etPeso);
        etAltura = findViewById(R.id.etAltura);
        btnCalcular = findViewById(R.id.btnCalcular);
        btnHistorial = findViewById(R.id.btnHistorial);
        tvResultadoActual = findViewById(R.id.tvResultadoActual);
        tvInterpretacion = findViewById(R.id.tvInterpretacion);
        tvHistorialResultados = findViewById(R.id.tvHistorialResultados);
    }

    private void setupCheckBoxes() {
        checkBoxHombre.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxMujer.setChecked(false);
            }
        });

        checkBoxMujer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxHombre.setChecked(false);
            }
        });
    }

    private void calcularIMC() {
        if (!validarEntradas()) {
            return;
        }

        double peso = Double.parseDouble(etPeso.getText().toString());
        double altura = Double.parseDouble(etAltura.getText().toString()) / 100; // convertir cm a m
        double imc;

        // Calcular IMC según género
        if (checkBoxMujer.isChecked()) {
            imc = (peso / (altura * altura)) * 0.9; // Factor de ajuste para mujeres
        } else {
            imc = peso / (altura * altura);
        }

        String interpretacion = interpretarIMC(imc);
        
        // Mostrar resultado actual
        tvResultadoActual.setText(String.format(Locale.getDefault(), "IMC: %.2f", imc));
        tvInterpretacion.setText(interpretacion);

        // Guardar resultado
        guardarResultado(imc, interpretacion);
    }

    private boolean validarEntradas() {
        if (!checkBoxHombre.isChecked() && !checkBoxMujer.isChecked()) {
            Toast.makeText(this, "Por favor seleccione un género", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etPeso.getText().toString().isEmpty() || etAltura.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String interpretarIMC(double imc) {
        if (imc < 18.5) return "Bajo peso";
        if (imc < 24.9) return "Peso normal";
        if (imc < 29.9) return "Sobrepeso";
        if (imc < 34.9) return "Obesidad grado 1";
        if (imc < 39.9) return "Obesidad grado 2";
        return "Obesidad grado 3";
    }

    private void guardarResultado(double imc, String interpretacion) {
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        
        Map<String, Object> imcData = new HashMap<>();
        imcData.put("imc", imc);
        imcData.put("interpretacion", interpretacion);
        imcData.put("fecha", fecha);
        imcData.put("genero", checkBoxHombre.isChecked() ? "Hombre" : "Mujer");
        imcData.put("peso", Double.parseDouble(etPeso.getText().toString()));
        imcData.put("altura", Double.parseDouble(etAltura.getText().toString()));

        mDatabase.child(currentUser)
                .push()
                .setValue(imcData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Resultado guardado exitosamente", Toast.LENGTH_SHORT).show();
                    cargarHistorial(); // Recargar historial después de guardar
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cargarHistorial() {
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar mensaje de carga
        tvHistorialResultados.setText("Cargando historial...");

        DatabaseReference userRef = mDatabase.child(currentUser);
        userRef.get()  // Removido orderByChild("fecha") ya que no es necesario
                .addOnSuccessListener(dataSnapshot -> {
                    StringBuilder historial = new StringBuilder();
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        try {
                            // Convertir los registros a una lista para poder ordenarlos
                            List<Map<String, Object>> listaRegistros = new ArrayList<>();
                            
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Map<String, Object> registro = (Map<String, Object>) snapshot.getValue();
                                if (registro != null) {
                                    listaRegistros.add(registro);
                                }
                            }

                            // Ordenar por fecha (más reciente primero)
                            Collections.sort(listaRegistros, (r1, r2) -> {
                                String fecha1 = (String) r1.get("fecha");
                                String fecha2 = (String) r2.get("fecha");
                                return fecha2.compareTo(fecha1); // Orden descendente
                            });

                            // Construir el historial con los registros ordenados
                            for (Map<String, Object> registro : listaRegistros) {
                                try {
                                    String fecha = (String) registro.get("fecha");
                                    Object imcObj = registro.get("imc");
                                    Double imc = (imcObj instanceof Double) ? (Double) imcObj : 
                                               Double.parseDouble(String.valueOf(imcObj));
                                    String interpretacion = (String) registro.get("interpretacion");
                                    Object pesoObj = registro.get("peso");
                                    Double peso = (pesoObj instanceof Double) ? (Double) pesoObj : 
                                                Double.parseDouble(String.valueOf(pesoObj));
                                    Object alturaObj = registro.get("altura");
                                    Double altura = (alturaObj instanceof Double) ? (Double) alturaObj : 
                                                  Double.parseDouble(String.valueOf(alturaObj));
                                    String genero = (String) registro.get("genero");

                                    historial.append("Fecha: ").append(fecha)
                                            .append("\nGénero: ").append(genero)
                                            .append("\nPeso: ").append(String.format(Locale.getDefault(), "%.1f kg", peso))
                                            .append("\nAltura: ").append(String.format(Locale.getDefault(), "%.1f cm", altura))
                                            .append("\nIMC: ").append(String.format(Locale.getDefault(), "%.2f", imc))
                                            .append("\nInterpretación: ").append(interpretacion)
                                            .append("\n----------------------------------------\n\n");
                                } catch (Exception e) {
                                    Log.e("Calculadora_IMC", "Error al procesar registro individual: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Calculadora_IMC", "Error al procesar registros: " + e.getMessage());
                            Toast.makeText(this, "Error al procesar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    if (historial.length() == 0) {
                        tvHistorialResultados.setText("No hay registros previos");
                    } else {
                        tvHistorialResultados.setText(historial.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Calculadora_IMC", "Error al cargar historial: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvHistorialResultados.setText("Error al cargar historial");
                });
    }
}