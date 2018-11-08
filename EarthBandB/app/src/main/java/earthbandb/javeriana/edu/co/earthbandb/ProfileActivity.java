package earthbandb.javeriana.edu.co.earthbandb;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    TextView TextNombre;
    ImageButton ImgBtnChat;
    ImageButton ImgBtnProfile;
    ImageButton ImgBtnAnadir;
    ImageButton ImgBtnReservas;
    ImageButton ImgBtnClientes;
    ImageButton ImgBtnSettings;
    ImageButton ImgBtnSoporte;
    TextView editarPerfilTextView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    Usuario user;
    String idUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        idUser=getIntent().getExtras().getString("id");
        TextNombre = (TextView) findViewById(R.id.nombreTextView);
        ImgBtnChat=(ImageButton) findViewById(R.id.imgBtnChat);
        ImgBtnProfile=(ImageButton)findViewById(R.id.imgBtnProfile);
        ImgBtnAnadir=(ImageButton)findViewById(R.id.imgBtnAñadir);
        ImgBtnReservas=(ImageButton)findViewById(R.id.imgBtnReservas);
        ImgBtnClientes=(ImageButton)findViewById(R.id.imgBtnClientes);
        ImgBtnSettings=(ImageButton)findViewById(R.id.imgBtnSettings);
        ImgBtnSoporte=(ImageButton)findViewById(R.id.imgBtnSoporte);
        editarPerfilTextView=(TextView)findViewById(R.id.editarPerfilTextView);
        //Se carga la informacion del usuario que inicia sesion
        myRef = database.getReference("usuarios/"+idUser);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(Usuario.class);
                //Toast.makeText(ProfileActivity.this,"Reacciona2... " + user.getNombre(),Toast.LENGTH_LONG).show();
                TextNombre.setText(user.getNombre()+" "+user.getApellido());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        //Termina de cargar la informacion

        //cargar la imagen
        FirebaseStorage storage= FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://earthbandb-62fc2.appspot.com");

// Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child("users/"+idUser);

        storageRef.child("users/"+idUser).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        //Termina de cargar la imagen

        //Listener para todos los botones
        ImgBtnChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this,"Listener Chat" ,Toast.LENGTH_LONG).show();
            }
        });
        ImgBtnProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(ProfileActivity.this,ProfileEditActivity.class);
                intent.putExtra("correo",user.getEmail());
                intent.putExtra("nombre",user.getNombre());
                intent.putExtra("id",idUser);
                startActivity(intent);
            }
        });
        ImgBtnAnadir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Listener agregar alojamiento
                Intent intent=new Intent(ProfileActivity.this,agregarAlojamientoActivity.class);
                startActivity(intent);
            }
        });
        ImgBtnReservas.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Listener ver reservas
                Toast.makeText(ProfileActivity.this,"Listener Ver Reservas" ,Toast.LENGTH_LONG).show();
            }
        });
        ImgBtnClientes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Listener ver Clientes
                Toast.makeText(ProfileActivity.this,"Listener Ver Clientes" ,Toast.LENGTH_LONG).show();
            }
        });
        ImgBtnSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Listener settings
                Toast.makeText(ProfileActivity.this,"Listener settings" ,Toast.LENGTH_LONG).show();
            }
        });
        ImgBtnSoporte.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Listener soporte
                Toast.makeText(ProfileActivity.this,"Listener soporte" ,Toast.LENGTH_LONG).show();
            }
        });
        editarPerfilTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Listener editar perfil
                Toast.makeText(ProfileActivity.this,"Listener editar Perfil" ,Toast.LENGTH_LONG).show();
            }
        });
        //Termina la implementacion de los listener
    }
}
