package com.gordos.earthbnb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gordos.earthbnb.modelo.Usuario;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    // Constantes de permisos
    static final int PERMISO_CAMARA = 1;
    static final int PERMISO_ALMACENAMIENTO = 2;

    // Constantes
    private static final String TAG = "Resultado: ";
    public static final String PATH_USUARIOS = "usuarios/";

    // Autenticación con Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private StorageReference imageRef;

    // Variables de los elementos visuales
    private EditText et_nombre;
    private EditText et_apellido;
    private EditText et_correo;
    private EditText et_contrasena;
    private Button btn_registrarme;
    private Button btn_galeria;
    private Button btn_camara;
    private ImageView img_foto_perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de los elementos visuales
        et_nombre = (EditText) findViewById(R.id.et_nombre);
        et_apellido = (EditText) findViewById(R.id.et_apellido);
        et_correo = (EditText) findViewById(R.id.et_correo);
        et_contrasena = (EditText) findViewById(R.id.et_contrasena);
        btn_registrarme = (Button) findViewById(R.id.btn_registrarme);
        btn_galeria = (Button) findViewById(R.id.btn_galeria);
        btn_camara = (Button) findViewById(R.id.btn_camara);
        img_foto_perfil = (ImageView) findViewById(R.id.img_foto_perfil);

        // Inicialización de Firebase
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Solicitud de permisos

        // Métodos onClick de los botones
        btn_registrarme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarUsuario();
            }
        });

        btn_galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarPermisoAlmacenamiento();
                if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent seleccionarImagen = new Intent(Intent.ACTION_PICK);
                    seleccionarImagen.setType("image/*");
                    startActivityForResult(seleccionarImagen, PERMISO_ALMACENAMIENTO);
                }
            }
        });

        btn_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarPermisoCamara();
                if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(tomarFoto.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(tomarFoto, PERMISO_CAMARA);
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Métodos de validación de la autenticación

    /**
     * Valida si los campos de autenticación están vacíos o bien diligenciados.
     *
     * @return boolean: Si el formato ha sido llenado correctamente o no.
     */
    private boolean validateForm() {
        boolean valido = true;
        String correo = et_correo.getText().toString();
        if (TextUtils.isEmpty(correo)) {
            et_correo.setError("¡Este campo es requerido!");
            valido = false;
        } else if (!esElCorreoValido(correo)) {
            valido = false;
            et_correo.setError("El correo es inválido...");
        } else {
            et_correo.setError(null);
        }
        String contrasena = et_contrasena.getText().toString();
        if (TextUtils.isEmpty(contrasena)) {
            et_contrasena.setError("¡Este campo es requerido!");
            valido = false;
        } else {
            et_contrasena.setError(null);
        }
        String nombre = et_nombre.getText().toString();
        if (TextUtils.isEmpty(nombre)) {
            et_nombre.setError("¡Este campo es requerido!");
            valido = false;
        } else {
            et_nombre.setError(null);
        }
        String apellido = et_apellido.getText().toString();
        if (TextUtils.isEmpty(apellido)) {
            et_apellido.setError("¡Este campo es requerido!");
            valido = false;
        } else {
            et_apellido.setError(null);
        }
        return valido;
    }

    /**
     * @param correo Correo electrónico a validar
     * @return boolean: Retorna si el correo está bien escrito o no
     */
    private boolean esElCorreoValido(String correo) {
        boolean esValido = true;
        if (!correo.contains("@") || !correo.contains(".") || correo.length() < 5)
            esValido = false;
        return esValido;
    }

    /**
     * Método que realiza el inicio de sesión de un usuario.
     */
    protected void registrarUsuario() {
        if (validateForm()) {
            final String correo = et_correo.getText().toString();
            String contrasena = et_contrasena.getText().toString();
            mAuth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String nombre = et_nombre.getText().toString();
                        String apellido = et_apellido.getText().toString();
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                            upcrb.setDisplayName(nombre + " " + apellido);

                            imageRef = storageRef.child(correo + ".jpg");

                            Bitmap bitmap = ((BitmapDrawable) img_foto_perfil.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            UploadTask uploadTask = imageRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "Falla");
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.i(TAG, "NO falla");
                                }
                            });

                            String urlImg = "https://firebasestorage.googleapis.com/v0/b/earthbnb-8e9db.appspot.com/o/" + correo + ".jpg";
                            upcrb.setPhotoUri(Uri.parse(urlImg));

                            // Agregar usuario a la base de datos
                            Usuario nuevoUsuario = new Usuario();
                            nuevoUsuario.setNombre(et_nombre.getText().toString());
                            nuevoUsuario.setApellido(et_apellido.getText().toString());
                            nuevoUsuario.setUrlFoto(urlImg);
                            nuevoUsuario.setCorreo(et_correo.getText().toString());

                            databaseRef = database.getReference("usuarios");
                            String key = databaseRef.push().getKey();
                            databaseRef = database.getReference(PATH_USUARIOS + key);
                            databaseRef.setValue(nuevoUsuario);

                            Toast.makeText(RegisterActivity.this, key, Toast.LENGTH_SHORT).show();

                            user.updateProfile(upcrb.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "User profile updated.");
                                    } else {
                                        Log.i(TAG, "User profile NOT updated.");
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.msg_error_autenticacion + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, task.getException().getMessage());
                    }
                }
            });
        }
    }

    // Solicitud de permisos

    /**
     * Método que solicita el permiso para usar la cámara.
     */
    private void solicitarPermisoCamara() {

        // Permiso cámara
        int validadorPermiso = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        if (validadorPermiso != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISO_CAMARA);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Es necesario el uso de la cámara", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISO_CAMARA);
            }
        } else {
            Log.i("PERMISO CÁMARA:", "Se le dio el permiso");
        }
    }

    /**
     * Método que solicita el permiso de almacenamiento.
     */
    private void solicitarPermisoAlmacenamiento() {

        // Permiso almacenamiento
        int validadorPermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (validadorPermiso != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISO_ALMACENAMIENTO);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Es necesario el uso de la galeria", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISO_ALMACENAMIENTO);
            }
        } else {
            Log.i("PERMISO ALMACENAMIENTO:", "Se le dio el permiso");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISO_CAMARA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(tomarFoto.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(tomarFoto, PERMISO_CAMARA);
                    }
//                    Log.i("PERMISO CÁMARA:", "Permiso concedido");
                } else {
                    // permission denied, disable functionality that depends on this permission.
                    Log.i("PERMISO CÁMARA:", "Permiso denegado");
                }
                return;
            }
            case PERMISO_ALMACENAMIENTO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent seleccionarImagen = new Intent(Intent.ACTION_PICK);
                    seleccionarImagen.setType("image/*");
                    startActivityForResult(seleccionarImagen, PERMISO_ALMACENAMIENTO);
//                    Log.i("PERMISO ALMACENAMIENTO:", "Permiso concedido");
                } else {
                    // permission denied, disable functionality that depends on this permission.
                    Log.i("PERMISO ALMACENAMIENTO:", "Permiso denegado");
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PERMISO_CAMARA: {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    img_foto_perfil.setImageBitmap(imageBitmap);
//                    Toast.makeText(this, "Se ha autorizado el uso de la cámara", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(this, "No hay acceso a la cámara", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case PERMISO_ALMACENAMIENTO: {
                if (resultCode == RESULT_OK) {
//                    Toast.makeText(this, "Se ha autorizado el uso del almacenamiento", Toast.LENGTH_LONG).show();
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        img_foto_perfil.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
//                    Toast.makeText(this, "No hay acceso al almacenamiento", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


}
