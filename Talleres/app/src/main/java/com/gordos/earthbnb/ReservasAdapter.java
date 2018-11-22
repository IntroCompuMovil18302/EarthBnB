package com.gordos.earthbnb;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gordos.earthbnb.modelo.Alojamiento;
import com.gordos.earthbnb.modelo.Comentario_Calificacion;
import com.gordos.earthbnb.modelo.ReservaCliente;
import com.gordos.earthbnb.modelo.Usuario;

import java.util.List;

public class ReservasAdapter extends ArrayAdapter<ReservaCliente> {

    TextView textViewDescripcionAdapter;
    TextView textViewTipoAdapter;
    TextView textViewNumAdapter;
    TextView textViewFechaAdapter;


    public ReservasAdapter(Context context, List<ReservaCliente> datos){
        super(context, R.layout.reservas_adapter, datos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReservaCliente reservaCliente = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.reservas_adapter, parent, false);
        }

        textViewDescripcionAdapter = convertView.findViewById(R.id.textViewDescripcionAdapter);
        textViewTipoAdapter = convertView.findViewById(R.id.textViewTipoAdapter);
        textViewNumAdapter = convertView.findViewById(R.id.textViewNumAdapter);
        textViewFechaAdapter = convertView.findViewById(R.id.textViewFechaAdapter);

        textViewDescripcionAdapter.setText(reservaCliente.getDescripcion());
        textViewTipoAdapter.setText(reservaCliente.getTipo());
        textViewNumAdapter.setText(String.valueOf(position+1));
        textViewFechaAdapter.setText(reservaCliente.getFecha());

        return convertView;
    }
}
