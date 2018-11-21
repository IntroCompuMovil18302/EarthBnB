package com.gordos.earthbnb;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gordos.earthbnb.modelo.Alojamiento;
import com.gordos.earthbnb.modelo.Comentario_Calificacion;
import com.gordos.earthbnb.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

public class ClientRatingActivity extends AppCompatActivity {

    //PATHS
    private static final String PATH_ALOJAMIENTOS = "alojamientos/";
    private static final String PATH_CALIFICACIONES = "calificaciones/";

    //Elementos visuales
    RatingBar rating_bar;
    EditText et_comentarios_calificacion;
    Button buttonEnviarCalificacion;
    private DatabaseReference databaseRef;
    private FirebaseDatabase database;
    TextView textViewSetOn;

    Alojamiento alojamiento;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_rating);

        mAuth = FirebaseAuth.getInstance();

        rating_bar = (RatingBar) findViewById(R.id.rating_bar) ;
        et_comentarios_calificacion = (EditText) findViewById(R.id.et_comentarios_calificacion);
        buttonEnviarCalificacion = (Button) findViewById(R.id.buttonEnviarCalificacion);
        textViewSetOn= (TextView) findViewById(R.id.textViewSetOn);

        buttonEnviarCalificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Log_Calificar","Button");

                databaseRef = FirebaseDatabase.getInstance().getReference();
                Intent intent = getIntent();
                database = FirebaseDatabase.getInstance();

                final String idAlojamiento= (String) intent.getExtras().get("alojamiento");

                databaseRef = database.getReference("usuarios/" + mAuth.getUid());
                databaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1) {
                        final Usuario usuarioActual = dataSnapshot1.getValue(Usuario.class);
                        Log.d("Log_Calificar",usuarioActual.getNombre());

                        //Buscar Alojamiento
                        databaseRef = database.getReference(PATH_ALOJAMIENTOS);
                        databaseRef.child(idAlojamiento)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot2) {
                                        if (dataSnapshot2.exists()) {
                                            alojamiento = dataSnapshot2.getValue(Alojamiento.class);

                                            Log.d("Log_Calificar",alojamiento.getDescripcion());

                                            Comentario_Calificacion comentario_calificacion= new Comentario_Calificacion(et_comentarios_calificacion.getText().toString(), rating_bar.getRating(), usuarioActual.getNombre());

                                            //Crear rama en calificaciones del alojamiento y colocar rating nuevo
                                            databaseRef = database.getReference(PATH_CALIFICACIONES + alojamiento.getIdAlojamiento());
                                            String comentario_calificacionKey = databaseRef.push().getKey();
                                            databaseRef = database.getReference(PATH_CALIFICACIONES + alojamiento.getIdAlojamiento() + "/" + comentario_calificacionKey);
                                            databaseRef.setValue(comentario_calificacion);

                                            //Hacer promedio y colocarlo en el alojamiento
                                            databaseRef = FirebaseDatabase.getInstance().getReference();

                                            //Buscar cada calificacion y hacer el promedio de calificaciones
                                            databaseRef.child(PATH_CALIFICACIONES).child(idAlojamiento)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot3) {
                                                            if (dataSnapshot3.exists()) {
                                                                double contTotal= 0, suma= 0;
                                                                for (DataSnapshot valorCalificacion : dataSnapshot3.getChildren()) {
                                                                    ++contTotal;
                                                                    suma += valorCalificacion.child("calificacion").getValue(Double.class);
                                                                }

                                                                Log.d("Log_Calificar",String.valueOf(suma/contTotal));

                                                                alojamiento.setCalificacion(suma/contTotal);

                                                                databaseRef = database.getReference(PATH_ALOJAMIENTOS + alojamiento.getIdAlojamiento());
                                                                databaseRef.setValue(alojamiento);
                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                        }
                                                    });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


                Intent intent2 = new Intent(ClientRatingActivity.this, MapsActivity.class);
                startActivity(intent2);
            }
        });

        rating_bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                textViewSetOn.setText(String.valueOf(v));
                switch ((int) ratingBar.getRating()) {
                    case 1:
                        textViewSetOn.setText("Muy malo");
                        break;
                    case 2:
                        textViewSetOn.setText("Malo");
                        break;
                    case 3:
                        textViewSetOn.setText("Necesita mejorar");
                        break;
                    case 4:
                        textViewSetOn.setText("Bueno");
                        break;
                    case 5:
                        textViewSetOn.setText("Excelente, lo amé");
                        break;
                    default:
                        textViewSetOn.setText("");
                }
            }
        });

    }

}
