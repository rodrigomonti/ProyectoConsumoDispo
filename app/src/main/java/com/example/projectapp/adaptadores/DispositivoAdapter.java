package com.example.projectapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.Dispositivo;
import com.example.projectapp.R;

import java.util.ArrayList;

public class DispositivoAdapter extends RecyclerView.Adapter<DispositivoAdapter.ViewHolder> {

    private ArrayList<Dispositivo> dispositivos;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public DispositivoAdapter(Context context, ArrayList<Dispositivo> dispositivos) {
        this.context = context;
        this.dispositivos = dispositivos;
    }

    // ViewHolder para contener las vistas de cada elemento en el RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewNombre;
        public TextView textViewCategoria;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.txtNombre);
            textViewCategoria = itemView.findViewById(R.id.txtCategoria);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout del elemento y crear el ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dispositivo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener el objeto Dispositivo para esta posición
        Dispositivo dispositivo = dispositivos.get(position);

        // Establecer los valores de las vistas con los datos del dispositivo
        if (dispositivo != null) {
            holder.textViewNombre.setText(dispositivo.getNombre());
            holder.textViewCategoria.setText(dispositivo.getCategoria());

            // Agrega el clic al elemento
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(dispositivo);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dispositivos.size();
    }

    // Interfaz para manejar clics en los elementos
    public interface OnItemClickListener {
        void onItemClick(Dispositivo dispositivo);
    }

    // Método para establecer el escuchador de clic externamente
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}



