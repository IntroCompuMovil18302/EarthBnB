package com.gordos.earthbnb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gordos.earthbnb.modelo.Comentario_Calificacion;

import java.util.List;

public class ComentariosAdapter extends ArrayAdapter<Comentario_Calificacion> {

    TextView textViewNombreComentario;
    TextView textViewComentario;
    TextView textViewCalificacion;

    public ComentariosAdapter(Context context, List<Comentario_Calificacion> datos){
        super(context, R.layout.comentarios_adapter, datos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comentario_Calificacion comentario_calificacion = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comentarios_adapter, parent, false);
        }

        textViewNombreComentario = convertView.findViewById(R.id.textViewNombreComentario);
        textViewComentario = convertView.findViewById(R.id.textViewComentario);
        textViewCalificacion = convertView.findViewById(R.id.textViewCalificacion);

        textViewNombreComentario.setText(comentario_calificacion.getNombre());
        textViewComentario.setText(comentario_calificacion.getComentario());
        textViewCalificacion.setText(String.valueOf(comentario_calificacion.getCalificacion()));

        return convertView;
    }
}
