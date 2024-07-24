package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Consumo extends AppCompatActivity {

    private TextView txtNombre, txtCategoria, txtCurrent, txtVoltage, txtConsumoTotal;
    private ImageButton btnVolver;
    private double consumoTotal = 0.0;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumo);

        txtNombre = findViewById(R.id.nombre);
        txtCategoria = findViewById(R.id.categoria);
        btnVolver = findViewById(R.id.btnVolver);
        txtCurrent = findViewById(R.id.txtCurrent);
        txtVoltage = findViewById(R.id.txtVoltage);
        txtConsumoTotal = findViewById(R.id.txtConsumoTotal);

        String nombreValue = getIntent().getStringExtra("nombre");
        String categoriaValue = getIntent().getStringExtra("categoria");

        if (nombreValue != null && categoriaValue != null) {
            txtNombre.setText(nombreValue);
            txtCategoria.setText(categoriaValue);
        }

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
                Toast.makeText(Consumo.this, "Error al obtener datos de Sensores", Toast.LENGTH_SHORT).show();
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                txtConsumoTotal.setText("Consumo total: " + consumoTotal);
                handler.postDelayed(this, 40 * 1000); // 2 minutos en milisegundos
            }
        };
        handler.postDelayed(runnable, 4 * 1000); // 2 minutos en milisegundos

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Consumo.this, RegistroDispositivo.class));
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

            double umbral = 100; // Establece el umbral según tus necesidades
            if (consumoTotal > umbral) {
                // Mostrar notificación de consumo alto
                mostrarNotificacion("Consumo alto", "El consumo total superó el umbral establecido");
            }
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}




