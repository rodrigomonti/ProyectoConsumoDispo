package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaDispositivos extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Spinner spinnerDispositivos;
    private TextView textViewInfoDispositivo, txtConsumoTotal, txtCurrent, txtVoltage, txtAhorro;
    private ImageButton btnVolver;
    private Handler handler;
    private Runnable runnable;
    private double consumoTotal = 0.0;
    private double ahorroAcumulado = 0.0;
    private List<Dispositivo> dispositivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_dispositivos);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Dispositivo");
        spinnerDispositivos = findViewById(R.id.spinnerDispositivos);
        textViewInfoDispositivo = findViewById(R.id.textViewInfoDispositivo);
        txtConsumoTotal = findViewById(R.id.txtConsumoTotal);
        btnVolver = findViewById(R.id.btnVolver);
        txtCurrent = findViewById(R.id.txtCurrent);
        txtVoltage = findViewById(R.id.txtVoltage);
        txtAhorro = findViewById(R.id.txtAhorro);
        dispositivos = new ArrayList<>();

        // Configurar adaptador para el Spinner
        final ArrayAdapter<Dispositivo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dispositivos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDispositivos.setAdapter(adapter);

        // Escuchar cambios en el nodo "Dispositivo"
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dispositivos.clear();
                for (DataSnapshot dispositivoSnapshot : dataSnapshot.getChildren()) {
                    Dispositivo dispositivo = dispositivoSnapshot.getValue(Dispositivo.class);
                    dispositivos.add(dispositivo);
                }
                adapter.notifyDataSetChanged(); // Notificar al adaptador sobre los cambios
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores si es necesario
            }
        });

        // Configurar listener para el Spinner
        spinnerDispositivos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Actualizar el TextView con la información del dispositivo seleccionado
                Dispositivo selectedDevice = (Dispositivo) parentView.getItemAtPosition(position);
                if (selectedDevice != null && selectedDevice.getId() != null) {
                    textViewInfoDispositivo.setText("Nombre: " + selectedDevice.getNombre() + "\nCategoria: " + selectedDevice.getCategoria());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Manejar caso en el que no se ha seleccionado nada
            }
        });

        DatabaseReference sensoresRef = FirebaseDatabase.getInstance().getReference().child("Sensores");
        sensoresRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateSensorValues(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateSensorValues(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ListaDispositivos.this, "Error al obtener datos de Sensores", Toast.LENGTH_SHORT).show();
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                txtConsumoTotal.setText("Consumo total: " + consumoTotal);
                txtAhorro.setText("Ahorro acumulado: " + ahorroAcumulado);
                handler.postDelayed(this, 40 * 1000); // 2 minutos en milisegundos
            }
        };
        handler.postDelayed(runnable, 4 * 1000); // 2 minutos en milisegundos

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ListaDispositivos.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void updateSensorValues(DataSnapshot dataSnapshot) {
        String sensorName = dataSnapshot.getKey();
        String currentValue = dataSnapshot.child("mediciones").child("Current").getValue(String.class);
        String voltageValue = dataSnapshot.child("mediciones").child("Voltage").getValue(String.class);

        if (sensorName != null) {
            if (sensorName.equals("ACS712") && currentValue != null) {
                txtCurrent.setText("Current: " + currentValue);
            } else if (sensorName.equals("ZMPT101B") && voltageValue != null) {
                txtVoltage.setText("Voltage: " + voltageValue);
            }
        }

        // Verificar si los valores no son nulos antes de convertirlos a double
        if (currentValue != null && voltageValue != null) {
            double current = Double.parseDouble(currentValue.trim());
            double voltage = Double.parseDouble(voltageValue.trim());
            double consumoSensor = current * voltage;
            consumoTotal += consumoSensor;

            // Calcular ahorro incremental (ajusta la fórmula según tus necesidades)
            double ahorroIncremental = consumoSensor * 0.10; // 10% del consumo
            ahorroAcumulado += ahorroIncremental;
        }
    }
}





