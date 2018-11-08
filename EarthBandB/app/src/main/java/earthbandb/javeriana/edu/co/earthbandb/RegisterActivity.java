package earthbandb.javeriana.edu.co.earthbandb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {
    Button registrar;
    EditText TextEmail;
    EditText TextPassword;
    EditText TextNombre;
    EditText TextApellido;
    EditText TextUsername;
    EditText TextVpass;
    ImageButton ImgBtnUsuario;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    DatabaseReference myRef;
    FirebaseDatabase database;
    RadioButton radBtnAdmn;
    RadioButton radBtnCmp;
    String tipoUser;
    Uri selectedImage;
    FirebaseStorage storage;
    StorageReference storageReference;
    private static final int SELECT_FILE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registrar=(Button) findViewById(R.id.btnRegistrar);
        TextEmail=(EditText) findViewById(R.id.emailEditText);
        TextPassword=(EditText) findViewById(R.id.passEditText);
        TextVpass=(EditText) findViewById(R.id.verifyEditText);
        TextNombre=(EditText) findViewById(R.id.NombreEditText);
        TextApellido=(EditText) findViewById(R.id.ApellidoeditText);
        TextUsername=(EditText) findViewById(R.id.usernameEditText);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance();
        radBtnAdmn = (RadioButton)findViewById(R.id.radio_arrendatario);
        radBtnCmp = (RadioButton)findViewById(R.id.radio_comprador);
        tipoUser="";
        ImgBtnUsuario=(ImageButton)findViewById(R.id.imgBtnUsuario);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        ImgBtnUsuario.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //registrarUsuarioAutenticacion();
                abrirGaleria(v);
            }
        });
        registrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registrarUsuarioAutenticacion();
            }
        });
    }
    public void onStart() {
        super.onStart();
    }

    public void registrarUsuarioDatabase(String idUser,String tipoUsuario){
        Usuario user;
        String nombre = TextNombre.getText().toString().trim();
        String apellido  = TextApellido.getText().toString().trim();
        String username = TextUsername.getText().toString().trim();
        String email = TextEmail.getText().toString().trim();

        user=new Usuario(nombre,apellido,email,idUser,username,tipoUsuario);
        myRef.setValue(user);

    }

    public void registrarUsuarioAutenticacion(){
        //Obtenemos el email y la contraseÒa desde las cajas de texto
        String email = TextEmail.getText().toString().trim();
        String password  = TextPassword.getText().toString().trim();
        String nombre = TextNombre.getText().toString().trim();
        String apellido = TextApellido.getText().toString().trim();
        String username = TextUsername.getText().toString().trim();
        String verify = TextVpass.getText().toString().trim();

        //Verificamos que las cajas de texto no esten vacÌas
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Se debe ingresar un email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Falta ingresar la contraseña",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(nombre)){
            Toast.makeText(this,"Falta ingresar el nombre",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(apellido)){
            Toast.makeText(this,"Falta ingresar el apellido",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"Falta ingresar el username",Toast.LENGTH_LONG).show();
            return;
        }
        if(!verify.equals(password)){
            Toast.makeText(this,"No coinciden las contraseñas",Toast.LENGTH_LONG).show();
            return;
        }
        //Verifica que este seleccionado uno de los tipos de usuario...
        if(!radBtnAdmn.isChecked() && !radBtnCmp.isChecked()){
            Toast.makeText(this,"Debe seleccionar un tipo de usuario",Toast.LENGTH_LONG).show();
            return;
        }


        progressDialog.setMessage("Realizando registro en linea...");

        progressDialog.show();


        //creating a new user on Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){
                            if(radBtnCmp.isChecked()){
                                tipoUser="Comprador";
                                //Toast.makeText(RegisterActivity.this,"ingresa a tipoUsuario: "+ tipoUser,Toast.LENGTH_LONG).show();
                            }
                            else{
                                tipoUser="Arrendatario";
                                //Toast.makeText(RegisterActivity.this,"ingresa a tipoUsuario: "+ tipoUser,Toast.LENGTH_LONG).show();
                            }
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this,"Se ha registrado el usuario con el email: "+ TextEmail.getText(),Toast.LENGTH_LONG).show();
                            //Toast.makeText(RegisterActivity.this,"Se ha registrado el usuario con id: "+user.getUid(),Toast.LENGTH_LONG).show();
                            myRef = database.getReference("usuarios/"+user.getUid());
                            registrarUsuarioDatabase(user.getUid(),tipoUser);
                            subirImagenStorage(user.getUid());
                            Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                            startActivity(intent);
                        }else{

                            Toast.makeText(RegisterActivity.this,"No se pudo registrar el usuario ",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
    public void abrirGaleria(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),
                SELECT_FILE);
    }

    //Re-escritura del metodo onActivity
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Uri selectedImageUri = null;


        String filePath = null;
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath=selectedImage.getPath();
                    if (requestCode == SELECT_FILE) {

                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                imageStream = getContentResolver().openInputStream(
                                        selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                            // Ponemos nuestro bitmap en un ImageView que tengamos en la vista
                            ImgBtnUsuario.setImageBitmap(bmp);

                        }
                    }
                }
                break;
        }
    }
    public void subirImagenStorage(String idAlojamiento){
        uploadImage(idAlojamiento);
    }

    //agrega la imagen al storage con id del alojamiento como nombre
    public void uploadImage(String idUsuario) {

        if(selectedImage != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("users/"+ idUsuario);
            ref.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

}
