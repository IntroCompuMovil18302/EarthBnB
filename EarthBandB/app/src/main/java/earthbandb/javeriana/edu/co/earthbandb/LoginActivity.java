package earthbandb.javeriana.edu.co.earthbandb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    Button btnIniciarSesion;
    TextView regisTextView;
    EditText emailEditText;
    EditText passEditText;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    Usuario auxUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnIniciarSesion=(Button) findViewById(R.id.iniciarSesionButton);
        regisTextView=(TextView) findViewById(R.id.crearCuentaTextView);
        emailEditText=(EditText) findViewById(R.id.emailEditText);
        passEditText=(EditText) findViewById(R.id.passEditText);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validarUsuarioAutenticacion();
            }
        });
        regisTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
    public void validarUsuarioAutenticacion(){

        //Obtenemos el email y la contraseÒa desde las cajas de texto
        String email = emailEditText.getText().toString().trim();
        String password  = passEditText.getText().toString().trim();

        //Verificamos que las cajas de texto no esten vacÌas
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Se debe ingresar un email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Falta ingresar la contraseña",Toast.LENGTH_LONG).show();
            return;
        }


        progressDialog.setMessage("Realizando validacion en linea...");

        progressDialog.show();


        //creating a new user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){

                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            myRef = database.getReference("usuarios/"+user.getUid());
                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    auxUser = dataSnapshot.getValue(Usuario.class);
                                    if(auxUser.getTipo().equals("Arrendatario")){
                                        Intent intent=new Intent(LoginActivity.this,ProfileActivity.class);
                                        intent.putExtra("id",user.getUid());
                                        startActivity(intent);
                                    }
                                    else{
                                        //Toast.makeText(LoginActivity.this,"La interfaz del cliente se encuentra en proceso "+ emailEditText.getText(),Toast.LENGTH_LONG).show();
                                        Intent intent=new Intent(LoginActivity.this,ClientProfileActivity.class);
                                        //intent.putExtra("id",user.getUid());
                                        startActivity(intent);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.out.println("The read failed: " + databaseError.getCode());
                                }
                            });

                            //Toast.makeText(LoginActivity.this,"Se ha iniciado sesion satisfactoriamente: "+ emailEditText.getText(),Toast.LENGTH_LONG).show();
                        }
                        else{
                            //Log.w(Tag, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "El email o la contraseña son incorrectos",
                                    Toast.LENGTH_SHORT).show();
                            //Toast.makeText(LoginActivity.this,"No se pudo iniciar sesion ",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }
}
