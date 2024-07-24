package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DetalleDispositivo extends AppCompatActivity {

    private EditText txtNombre;
    private EditText txtCategoria;
    private ImageView bluetoothIv;
    private FloatingActionButton fabEditar, fabEliminar;
    private Button btnConectar, btnDesconectar;
    private ImageButton btnVolver;
    private boolean dispositivoConectado = false;
    private String nombreDispositivo;
    private SharedPreferences preferences;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_dispositivo);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        preferences = getSharedPreferences("estado_dispositivo", MODE_PRIVATE);

        fabEditar = findViewById(R.id.fabEditar);
        fabEliminar = findViewById(R.id.fabEliminar);
        btnVolver = findViewById(R.id.btnVolver);
        btnConectar = findViewById(R.id.btnConectar);
        btnDesconectar = findViewById(R.id.btnDesconectar);

        txtNombre = findViewById(R.id.txtNombre);
        txtCategoria = findViewById(R.id.txtCategoria);
        bluetoothIv = findViewById(R.id.bluetoothIv);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            nombreDispositivo = bundle.getString("nombre");
            String categoria = bundle.getString("categoria");
            txtNombre.setText(nombreDispositivo);
            txtCategoria.setText(categoria);

            dispositivoConectado = preferences.getBoolean(nombreDispositivo, false);
            actualizarEstadoBluetooth();

            btnConectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Entro a:", "Boton conectar");

                    // Crear instancia de Dispositivo con Arduino y la información del Bundle
                    Dispositivo dispositivo = new Dispositivo(nombreDispositivo, nombreDispositivo, categoria);

                    // Registrar el dispositivo en Firebase Realtime Database
                    registrarDispositivoEnFirebase(dispositivo);

                    // Después de conectar el dispositivo, actualiza el estado
                    dispositivoConectado = true;
                    actualizarEstadoBluetooth();

                    String nombre = dispositivo.getNombre();
                    String categoria = dispositivo.getCategoria();

                    Intent intent = new Intent(DetalleDispositivo.this, Consumo.class);
                    intent.putExtra("nombre",nombre);
                    intent.putExtra("categoria",categoria);

                    startActivity(intent);
                }
            });

            btnDesconectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Llamar al método para desconectar y eliminar Arduino
                    desconectarYEliminarArduino();

                    // Después de desconectar el dispositivo, actualiza el estado
                    dispositivoConectado = false;
                    actualizarEstadoBluetooth();
                }
            });
        }

        fabEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetalleDispositivo.this, ModificarDispositivo.class)
                        .putExtra("nombre", txtNombre.getText().toString())
                        .putExtra("categoria", txtCategoria.getText().toString());
                startActivity(intent);
            }
        });

        fabEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idToDelete = txtNombre.getText().toString();

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dbref = db.getReference(Dispositivo.class.getSimpleName());

                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean dispositivoEncontrado = false;

                        for (DataSnapshot x : snapshot.getChildren()) {
                            Dispositivo dispositivo = x.getValue(Dispositivo.class);
                            if (dispositivo != null && idToDelete.equalsIgnoreCase(dispositivo.getNombre())) {
                                dispositivoEncontrado = true;

                                AlertDialog.Builder builder = new AlertDialog.Builder(DetalleDispositivo.this);
                                builder.setCancelable(false);
                                builder.setTitle("Pregunta");
                                builder.setMessage("¿Está Seguro(a) De Querer Eliminar El Registro?");

                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Puedes agregar lógica adicional si es necesario
                                    }
                                });

                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ocultarTeclado();
                                        x.getRef().removeValue();
                                        // Puedes agregar lógica adicional después de eliminar el dispositivo

                                        Intent intent = new Intent(DetalleDispositivo.this, RegistroDispositivo.class);

                                        startActivity(intent);
                                    }
                                });

                                builder.show();

                                break;
                            }
                        }

                        if (!dispositivoEncontrado) {
                            ocultarTeclado();
                            Toast.makeText(DetalleDispositivo.this, "ID (" + idToDelete + ") No Encontrado.\nImposible Eliminar!!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Manejar el error si es necesario
                    }
                });
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetalleDispositivo.this, RegistroDispositivo.class));
            }
        });
    }

    // Método para registrar el dispositivo en Firebase Realtime Database
    private void registrarDispositivoEnFirebase(Dispositivo dispositivo) {
        // Obtener un ID único para el dispositivo (puedes usar el nombre como ID)
        String dispositivoId = dispositivo.getId();

        // Crear una referencia a la ubicación del dispositivo en la base de datos
        DatabaseReference dispositivoRef = databaseReference.child("Dispositivo").child(dispositivoId);

        // Establecer el valor del dispositivo en la base de datos
        dispositivoRef.setValue(dispositivo)
                .addOnSuccessListener(aVoid -> {
                    // Registro exitoso
                    // Aquí puedes realizar cualquier acción adicional después de registrar en Firebase
                    Log.d("Registro en Firebase", "Registro exitoso");
                    Toast.makeText(DetalleDispositivo.this, "Dispositivo conectado con exito!", Toast.LENGTH_SHORT).show();
                    // También registra la clase Arduino dentro del nodo del dispositivo
                })
                .addOnFailureListener(e -> {
                    // Error en el registro
                    // Manejar el error según tus necesidades
                    Log.e("Registro en Firebase", "Error en el registro: " + e.getMessage());
                });
    }

    private void desconectarYEliminarArduino() {
        // Obtén el nombre del dispositivo desde el Bundle
        String nombreDispositivo = txtNombre.getText().toString();

        // Referencia a la ubicación del dispositivo en Firebase
        DatabaseReference dispositivoRef = databaseReference.child("Dispositivo").child(nombreDispositivo);

        // Eliminar la propiedad "Arduino" dentro del nodo del dispositivo
        dispositivoRef.child("arduino").removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Eliminación exitosa
                    Log.d("Desconexión de Arduino", "Eliminación exitosa de Arduino");
                    Toast.makeText(DetalleDispositivo.this, "Dispositivo desconectado con exito!", Toast.LENGTH_SHORT).show();
                    // Puedes realizar acciones adicionales después de desconectar y eliminar Arduino si es necesario
                })
                .addOnFailureListener(e -> {
                    // Error en la eliminación
                    Log.e("Desconexión de Arduino", "Error en la eliminación de Arduino: " + e.getMessage());
                });
    }

    private void actualizarEstadoBluetooth() {
        if (dispositivoConectado) {
            bluetoothIv.setImageResource(R.drawable.bluetooth_on);
        } else {
            bluetoothIv.setImageResource(R.drawable.bluetooth_off);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Guardar estado en SharedPreferences al pausar la actividad
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(nombreDispositivo, dispositivoConectado);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recuperar estado desde SharedPreferences al resumir la actividad
        dispositivoConectado = preferences.getBoolean(nombreDispositivo, false);
        actualizarEstadoBluetooth();
    }

    private void ocultarTeclado() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}