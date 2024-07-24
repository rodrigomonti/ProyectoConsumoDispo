package com.example.projectapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Dispositivo implements Parcelable {

    private String id; // Agregado el campo id
    private String nombre;
    private String categoria;

    public Dispositivo() {
    }

    public Dispositivo(String id, String nombre, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return this.nombre;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.nombre);
        dest.writeString(this.categoria);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.nombre = source.readString();
        this.categoria = source.readString();
    }

    protected Dispositivo(Parcel in) {
        this.id = in.readString();
        this.nombre = in.readString();
        this.categoria = in.readString();
    }

    public static final Parcelable.Creator<Dispositivo> CREATOR = new Parcelable.Creator<Dispositivo>() {
        @Override
        public Dispositivo createFromParcel(Parcel source) {
            return new Dispositivo(source);
        }

        @Override
        public Dispositivo[] newArray(int size) {
            return new Dispositivo[size];
        }
    };

}

