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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Spinner;
import android.widget.Toast;

import com.example.projectapp.adaptadores.DispositivoAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegistroDispositivo extends AppCompatActivity {

    private EditText nombre;
    private Button btnRegistrar;
    private ImageButton btnVolver;
    private Spinner spinnerCategoria;
    private RecyclerView listDispositivos;
    private SearchView buscarDispositivo;

    private ArrayList<Dispositivo> originalList = new ArrayList<>();

    // Lista para almacenar dispositivos
    private ArrayList<Dispositivo> listDis = new ArrayList<>();
    // Adaptador para la lista de dispositivos
    private DispositivoAdapter dispositivoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_dispositivo);

        nombre = findViewById(R.id.nombre);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        buscarDispositivo = findViewById(R.id.buscarDispo);
        btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistroDispositivo.this, MainActivity.class));
            }
        });

        // Obtener las categorías desde resources
        String[] categorias = getResources().getStringArray(R.array.categorias);

        // Añadir el prompt al inicio del array
        String[] categoriasConPrompt = new String[categorias.length + 1];
        categoriasConPrompt[0] = getString(R.string.select_category);
        System.arraycopy(categorias, 0, categoriasConPrompt, 1, categorias.length);

        // Crear un ArrayAdapter usando las categorías y un layout simple
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasConPrompt);

        // Configurar el layout a usar cuando se despliega la lista de opciones
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al Spinner
        spinnerCategoria.setAdapter(adapter);

        buscarDispositivo.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Puedes manejar la lógica de búsqueda aquí si es necesario
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Lógica para manejar cambios en el texto de búsqueda
                // Aquí puedes realizar la búsqueda y actualizar la lista según sea necesario
                buscarDispositivos(newText);

                // Si el texto de búsqueda está vacío y la lista temporal está vacía, restaura la lista original
                if (newText.isEmpty() && listDis.isEmpty() && !originalList.isEmpty()) {
                    // Limpiar la lista antes de asignar la nueva lista
                    listDis.clear();
                    // Agregar los dispositivos que coinciden con la búsqueda a la lista principal
                    listDis.addAll(originalList);
                    // Notificar al adaptador después de restaurar la lista original
                    dispositivoAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        btnRegistrar = findViewById(R.id.btnRegistrar);
        listDispositivos = findViewById(R.id.listDispositivos);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listDispositivos.setLayoutManager(layoutManager);

        // Inicializar el adaptador y asignarlo a la lista de dispositivos
        dispositivoAdapter = new DispositivoAdapter(this, listDis);
        listDispositivos.setAdapter(dispositivoAdapter);

        // Establece el escuchador de clic
        dispositivoAdapter.setOnItemClickListener(new DispositivoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Dispositivo dispositivo) {
                // Maneja el clic en el elemento aquí
                // Por ejemplo, puedes abrir una nueva actividad
                Intent intent = new Intent(RegistroDispositivo.this, DetalleDispositivo.class);
                intent.putExtra("nombre", dispositivo.getNombre());
                intent.putExtra("categoria",dispositivo.getCategoria());
                startActivity(intent);
            }
        });

        if (originalList.isEmpty()) {
            originalList.addAll(listDis);
        }

        botonRegistrar();
        listarDispositivos();
    }

    private void buscarDispositivos(String query) {
        Log.d("DEBUG", "Entró a buscarDispositivos");

        // Convertir la búsqueda a minúsculas para que la comparación sea insensible a mayúsculas
        String queryLower = query.toLowerCase();

        // Crear una nueva lista temporal para almacenar dispositivos que coinciden con la búsqueda
        ArrayList<Dispositivo> tempList = new ArrayList<>();

        if (query.isEmpty()) {
            // Si la búsqueda está vacía, restaurar la lista original
            listDis.clear();
            listDis.addAll(originalList);
            Log.d("DEBUG", "Restaurando lista original");
        } else {
            // Si hay una búsqueda, filtrar la lista según la búsqueda
            for (Dispositivo dispositivo : originalList) {
                // Lógica de búsqueda por nombre
                boolean coincideNombre = dispositivo.getNombre().toLowerCase().contains(queryLower);

                // Lógica de búsqueda por categoría
                boolean coincideCategoria = dispositivo.getCategoria().toLowerCase().contains(queryLower);

                // Verificar si el dispositivo coincide con la búsqueda por nombre o categoría
                if (coincideNombre || coincideCategoria) {
                    tempList.add(dispositivo);
                }
            }

            // Limpiar la lista antes de asignar la nueva lista
            listDis.clear();
            // Agregar los dispositivos que coinciden con la búsqueda a la lista principal
            listDis.addAll(tempList);
        }

        // Notificar al adaptador después de completar la búsqueda
        dispositivoAdapter.notifyDataSetChanged();

        // Imprimir información adicional para depuración
        Log.d("DEBUG", "Lista original: " + originalList.size());
        Log.d("DEBUG", "Lista después de la búsqueda: " + listDis.size());

        // Mostrar el mensaje solo si la búsqueda está vacía y no hay resultados
        if (query.isEmpty() && listDis.isEmpty()) {
            Toast.makeText(RegistroDispositivo.this, "Dispositivo no encontrado", Toast.LENGTH_SHORT).show();
            Log.d("DEBUG", "Dispositivo no encontrado");
        }

        Log.d("DEBUG", "Terminó buscarDispositivos");
    }

    private void botonRegistrar() {
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (nombre.getText().toString().trim().isEmpty() || spinnerCategoria.getSelectedItem().toString().trim().isEmpty()) {

                    ocultarTeclado();
                    Toast.makeText(RegistroDispositivo.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();

                } else {

                    String nombreTxt = nombre.getText().toString();
                    String categoriaTxt = spinnerCategoria.getSelectedItem().toString();

                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference dbref = db.getReference(Dispositivo.class.getSimpleName()); // Cambié Dispositivo.class.getSimpleName() por "dispositivos"

                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            boolean res = false;
                            for (DataSnapshot x : snapshot.getChildren()) {
                                if (x.child("nombre").getValue(String.class).equalsIgnoreCase(nombreTxt)) {
                                    res = true;
                                    ocultarTeclado();
                                    Toast.makeText(RegistroDispositivo.this, "Error. El Nombre (" + nombreTxt + ") Ya Existe!!", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }

                            if (!res) {
                                String dispositivoId = nombreTxt;
                                Dispositivo dispositivo = new Dispositivo(dispositivoId, nombreTxt, categoriaTxt);
                                dbref.child(dispositivoId).setValue(dispositivo);
                                ocultarTeclado();
                                Toast.makeText(RegistroDispositivo.this, "Dispositivo Registrado Correctamente!!", Toast.LENGTH_SHORT).show();
                                nombre.setText("");
                                spinnerCategoria.setSelection(0);

                                // Después de registrar el dispositivo con éxito, actualizar la lista de dispositivos
                                listarDispositivos();// Establecer la selección en la posición 0 o en la posición que desees
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Manejar el error si es necesario
                        }
                    });
                }
            }
        });
    }

    private void listarDispositivos() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbref = db.getReference(Dispositivo.class.getSimpleName());

        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpiar la lista original antes de agregar nuevos elementos
                originalList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Dispositivo dispositivo = dataSnapshot.getValue(Dispositivo.class);
                    originalList.add(dispositivo);
                }

                // Imprimir información adicional para depuración
                Log.d("DEBUG", "Lista original después de listarDispositivos: " + originalList.size());

                // Copiar la lista original a la lista de dispositivos principal
                listDis.clear();
                listDis.addAll(originalList);

                // Notificar al adaptador después de llenar la lista original
                dispositivoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG", "Error al leer dispositivos desde la base de datos: " + error.getMessage());
            }
        });
    }


    private void ocultarTeclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}


