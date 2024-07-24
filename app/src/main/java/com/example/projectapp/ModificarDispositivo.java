package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class ModificarDispositivo extends AppCompatActivity {

    Button btnModificar;
    EditText modificarNombre;
    Spinner modificarCategoriaSpinner;  // Cambiado de EditText a Spinner
    DatabaseReference dbref;
    ImageButton btnVolver;
    private String[] categorias = {"Categoría 1", "Categoría 2", "Categoría 3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_dispositivo);

        btnModificar = findViewById(R.id.btnModificar);
        modificarNombre = findViewById(R.id.modificarNombre);
        modificarCategoriaSpinner = findViewById(R.id.modificarCategoria);  // Usar Spinner en lugar de EditText
        btnVolver = findViewById(R.id.btnVolver);

        String[] categoriasArray = getResources().getStringArray(R.array.categorias);

        // Crear el adaptador con el array de categorías
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasArray);

        // Configurar el layout a usar cuando se despliega la lista de opciones
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al Spinner
        modificarCategoriaSpinner.setAdapter(adapter);

        dbref = FirebaseDatabase.getInstance().getReference(Dispositivo.class.getSimpleName());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String nombre = bundle.getString("nombre");
            String categoria = bundle.getString("categoria");

            Log.d("DEBUG", "Nombre recibido en ModificarDispositivo: " + nombre);
            Log.d("DEBUG", "Categoría recibida en ModificarDispositivo: " + categoria);

            modificarNombre.setText(nombre);

            // Seleccionar la categoría en el Spinner
            int categoriaPosition = obtenerPosicionCategoria(categoria);
            modificarCategoriaSpinner.setSelection(categoriaPosition);

            btnModificar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nuevoNombre = modificarNombre.getText().toString();
                    // Obtener la nueva categoría seleccionada del Spinner
                    String nuevaCategoria = modificarCategoriaSpinner.getSelectedItem().toString();

                    actualizarDispositivoEnFirebase(nombre, nuevoNombre, nuevaCategoria);

                    Intent intent = new Intent(ModificarDispositivo.this, RegistroDispositivo.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ModificarDispositivo.this, RegistroDispositivo.class));
            }
        });
    }

    private void actualizarDispositivoEnFirebase(String id, String nuevoNombre, String nuevaCategoria) {
        DatabaseReference dispositivoAntiguoRef = dbref.child(id);
        DatabaseReference nuevoDispositivoRef = dbref.child(nuevoNombre);

        // Eliminar el dispositivo antiguo
        dispositivoAntiguoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> eliminacionTask) {
                if (eliminacionTask.isSuccessful()) {
                    // Dispositivo antiguo eliminado correctamente, ahora agregar el nuevo
                    agregarDispositivoActualizado(nuevoDispositivoRef, nuevoNombre, nuevaCategoria);
                } else {
                    // Manejar el error al eliminar el dispositivo antiguo
                    Log.e("Firebase", "Error al eliminar dispositivo anterior: " + eliminacionTask.getException().getMessage());
                }
            }
        });
    }

    private void agregarDispositivoActualizado(DatabaseReference nuevoDispositivoRef, String nuevoNombre, String nuevaCategoria) {
        nuevoDispositivoRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                // Actualizar los valores del nuevo dispositivo
                currentData.child("nombre").setValue(nuevoNombre);
                currentData.child("categoria").setValue(nuevaCategoria);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed && error == null) {
                    // La transacción se completó correctamente
                    ocultarTeclado();
                    Toast.makeText(ModificarDispositivo.this, "Dispositivo actualizado correctamente", Toast.LENGTH_SHORT).show();
                    // Opcional: Puedes cerrar la actividad después de actualizar si lo deseas
                    finish();
                } else {
                    // Manejar el error en la transacción
                    Log.e("Firebase", "Error al agregar dispositivo actualizado: " + error.getMessage());
                }
            }
        });

    }

    private int obtenerPosicionCategoria(String categoria) {
        if (modificarCategoriaSpinner != null && modificarCategoriaSpinner.getAdapter() != null && modificarCategoriaSpinner.getSelectedItem() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) modificarCategoriaSpinner.getAdapter();
            return adapter.getPosition(categoria);
        } else {
            // Manejar la situación donde el adaptador o el elemento seleccionado son nulos
            // Intenta buscar la posición directamente en el array de categorías
            for (int i = 0; i < categorias.length; i++) {
                if (categoria.equals(categorias[i])) {
                    return i;
                }
            }
            // O proporciona un valor predeterminado según tu lógica
            return 0;
        }
    }


    private void ocultarTeclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}