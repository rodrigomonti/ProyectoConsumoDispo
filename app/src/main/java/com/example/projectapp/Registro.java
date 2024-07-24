package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Registro extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://projectapp-3c17c-default-rtdb.firebaseio.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        final EditText usuario = findViewById(R.id.loginUsuario);
        final EditText nombre = findViewById(R.id.nombre);
        final EditText email = findViewById(R.id.email);
        final EditText clave = findViewById(R.id.loginClave);
        final EditText confirmarClave = findViewById(R.id.confirmarClave);

        final Button btnRegistro = findViewById(R.id.btnRegistro);
        final TextView usuarioExistente = findViewById(R.id.usuarioExistente);


        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String usuarioTxt = usuario.getText().toString();
                final String nombreTxt = nombre.getText().toString();
                final String emailTxt = email.getText().toString();
                final String claveTxt = clave.getText().toString();
                final String confClaveTxt = confirmarClave.getText().toString();

                if(usuarioTxt.isEmpty() || nombreTxt.isEmpty() || emailTxt.isEmpty() || claveTxt.isEmpty() || confClaveTxt.isEmpty()){
                    Toast.makeText(Registro.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                }

                else if(!claveTxt.equals(confClaveTxt)){
                    Toast.makeText(Registro.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }

                else {

                    databaseReference.child("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.hasChild(usuarioTxt)){
                                Toast.makeText(Registro.this, "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                databaseReference.child("Usuarios").child(usuarioTxt).child("nombre").setValue(nombreTxt);
                                databaseReference.child("Usuarios").child(usuarioTxt).child("email").setValue(emailTxt);
                                databaseReference.child("Usuarios").child(usuarioTxt).child("clave").setValue(claveTxt);

                                Toast.makeText(Registro.this, "Usuario registrasado con exito", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }
        });

        usuarioExistente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}