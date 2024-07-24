package com.example.projectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.projectapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        TextView bienvenidaUsuario = findViewById(R.id.bienvenidaUsuario);

        bienvenidaUsuario.setText("BIENVENIDO " + nombreUsuario + "!");

        CardView agregarDispositivo = findViewById(R.id.cardAgregarDispo);
        CardView consumoDispositivo = findViewById(R.id.cardConsumo);
        CardView ahorroDispositivo = findViewById(R.id.cardAhorro);

        agregarDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistroDispositivo.class));
            }
        });

        consumoDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ListaDispositivos.class));
            }
        });

        ahorroDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Ahorro.class));
            }
        });

    }
}