package com.gordos.earthbnb;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Constantes
    private static final String TAG = "Resultado: ";

    // Autenticación con Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Variables de los elementos visuales
    private EditText et_correo;
    private EditText et_contrasena;
    private Button btn_ingreso;
    private Button btn_registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialización de los elementos visuales
        et_correo = (EditText) findViewById(R.id.et_correo);
        et_contrasena = (EditText) findViewById(R.id.et_contrasena);
        btn_ingreso = (Button) findViewById(R.id.btn_ingreso);
        btn_registro = (Button) findViewById(R.id.btn_registro);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Métodos onClick de los botones
        btn_ingreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarSesionDeUsuario();
            }
        });

        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RegisterActivity.class);
                startActivity(intent);
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
        } else if(!esElCorreoValido(correo)) {
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
        return valido;
    }

    /**
     * Método que realiza el inicio de sesión de un usuario.
     */
    protected void iniciarSesionDeUsuario() {
        if (validateForm()) {
            String correo = et_correo.getText().toString();
            String contrasena = et_contrasena.getText().toString();
            mAuth.signInWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG,"signInWithEmail:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.w(TAG,"signInWithEmail:failed", task.getException());
                        Toast.makeText(LoginActivity.this, R.string.msg_error_autenticacion, Toast.LENGTH_SHORT).show();
                        et_correo.setText("");
                        et_contrasena.setText("");
                    }
                }
            });
        }
    }

    /**
     *
     * @param correo Correo electrónico a validar
     * @return boolean: Retorna si el correo está bien escrito o no
     */
    private boolean esElCorreoValido(String correo) {
        boolean esValido = true;
        if (!correo.contains("@") || !correo.contains(".") || correo.length() < 5)
            esValido = false;
        return esValido;
    }

}
