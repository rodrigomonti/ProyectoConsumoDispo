package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://projectapp-3c17c-default-rtdb.firebaseio.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText loginUsuario = findViewById(R.id.loginUsuario);
        final EditText loginClave = findViewById(R.id.loginClave);
        final Button btnLogin = findViewById(R.id.btnLogin);
        final TextView nuevoUsuario = findViewById(R.id.nuevoUsuario);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String usuario = loginUsuario.getText().toString();
                final String clave = loginClave.getText().toString();

                if(usuario.isEmpty() || clave.isEmpty()){
                    Toast.makeText(Login.this,"Por favor ingresa tu usuario y contraseña", Toast.LENGTH_SHORT).show();
                }
                else {
                    databaseReference.child("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.hasChild(usuario)){

                                final String getClave = snapshot.child(usuario).child("clave").getValue(String.class);

                                if(getClave.equals(clave)){
                                    Toast.makeText(Login.this, "Acceso exitoso", Toast.LENGTH_SHORT).show();
                                    String nombreUsuario = snapshot.child(usuario).child("nombre").getValue(String.class);

                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    intent.putExtra("nombreUsuario", nombreUsuario);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(Login.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(Login.this, "Usuario incorrecto", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        nuevoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Registro.class));
            }
        });


    }
}