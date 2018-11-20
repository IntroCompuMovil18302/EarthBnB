package com.gordos.earthbnb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gordos.earthbnb.modelo.Alojamiento;

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

    //Variables globales
    private int promedio= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_rating);

        rating_bar = (RatingBar) findViewById(R.id.rating_bar) ;
        et_comentarios_calificacion = (EditText) findViewById(R.id.et_comentarios_calificacion);
        buttonEnviarCalificacion = (Button) findViewById(R.id.buttonEnviarCalificacion);

        buttonEnviarCalificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databaseRef = FirebaseDatabase.getInstance().getReference();
                Intent intent = getIntent();
                database = FirebaseDatabase.getInstance();

                //Buscar Alojamiento que llega por Intent
                databaseRef.child(PATH_ALOJAMIENTOS).child((String) intent.getExtras().get("alojamiento"))
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Alojamiento alojamiento = dataSnapshot.getValue(Alojamiento.class);

                                    //Crear rama en calificaciones del alojamiento y colocar rating nuevo
                                    databaseRef = database.getReference(PATH_CALIFICACIONES + alojamiento.getIdAlojamiento());
                                    databaseRef.setValue(rating_bar.getRating());

                                    //Hacer promedio y colocarlo en el alojamiento
                                    calificacionGlobal(alojamiento.getIdAlojamiento());
                                    alojamiento.setCalificacion(promedio);

                                    //Reemplazar alojamiento con nueva calificación
                                    databaseRef = database.getReference(PATH_ALOJAMIENTOS + alojamiento.getIdAlojamiento());
                                    databaseRef.setValue(alojamiento);

                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
            }
        });
    }

    public void calificacionGlobal(String idAlojamiento){

        databaseRef = FirebaseDatabase.getInstance().getReference();

        //Buscar cada calificacion y hacer el promedio de calificaciones
        databaseRef.child(PATH_CALIFICACIONES).child(idAlojamiento)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int contTotal= 0, suma= 0;
                            for (DataSnapshot valorCalificacion: dataSnapshot.getChildren()) {
                                Log.d("Log_Calificacion",String.valueOf(valorCalificacion.getValue(Integer.class)));
                                ++contTotal;
                                suma += valorCalificacion.getValue(Integer.class);
                            }
                            promedio = suma/contTotal;
                            Log.d("Log_Calificacion_Prom",String.valueOf(promedio));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

}
