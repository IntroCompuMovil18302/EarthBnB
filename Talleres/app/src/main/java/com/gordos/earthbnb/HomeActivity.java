package com.gordos.earthbnb;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gordos.earthbnb.modelo.Alojamiento;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Calendario
    public final Calendar c = Calendar.getInstance();
    private static final String CERO = "0";
    private static final String BARRA = "/";
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    private long fechaInicio;
    private long fechafin;

    private BitmapDrawable imagenCargada;

    private static final String PATH_ALOJAMIENTOS = "alojamientos/";
    private boolean esFechaInicio;

    // Variables menu
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // Autenticación con Firebase
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private StorageReference imageRef;

    //Variables de los elementos visuales
    private TextView et_verTipoAlojamiento;
    private TextView et_verPrecio;
    private TextView et_verNumHuespedes;
    private TextView et_verNumHabitaciones;
    private TextView et_verNumCamas;
    private TextView et_verNumBanios;
    private TextView et_verElementosBasicos;
    private TextView et_verWifi;
    private TextView et_verTV;
    private TextView et_verArmario;
    private TextView et_verEscritorio;
    private TextView et_verDesayuno;
    private TextView et_verAccesoCocina;
    private TextView et_descripcion_ver;
    private TextView tv_ver_fecha_inicio;
    private TextView tv_ver_fecha_fin;
    private TextView et_verBanioPrivado;
    private Button btn_arrendar_alojamiento_submit;
    private ImageButton btn_ver_fecha_inicio;
    private ImageButton btn_ver_fecha_fin;
    LinearLayout ly_fotos_alojamiento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        et_verTipoAlojamiento = (TextView) findViewById(R.id.et_verTipoAlojamiento);
        et_verPrecio = (TextView) findViewById(R.id.et_verPrecio);
        et_verNumHuespedes = (TextView) findViewById(R.id.et_verNumHuespedes);
        et_verNumHabitaciones = (TextView) findViewById(R.id.et_verNumHabitaciones);
        et_verNumCamas = (TextView) findViewById(R.id.et_verNumCamas);
        et_verNumBanios = (TextView) findViewById(R.id.et_verNumBanios);
        et_verElementosBasicos = (TextView) findViewById(R.id.et_verElementosBasicos);
        et_verWifi = (TextView) findViewById(R.id.et_verWifi);
        et_verTV = (TextView) findViewById(R.id.et_verTV);
        et_verArmario = (TextView) findViewById(R.id.et_verArmario);
        et_verEscritorio = (TextView) findViewById(R.id.et_verEscritorio);
        et_verDesayuno = (TextView) findViewById(R.id.et_verDesayuno);
        et_verAccesoCocina = (TextView) findViewById(R.id.et_verAccesoCocina);
        et_descripcion_ver = (TextView) findViewById(R.id.et_descripcion_ver);
        tv_ver_fecha_inicio = (TextView) findViewById(R.id.tv_ver_fecha_inicio);
        tv_ver_fecha_fin = (TextView) findViewById(R.id.tv_ver_fecha_fin);
        et_verBanioPrivado = (TextView) findViewById(R.id.et_verBanioPrivado);

        btn_ver_fecha_inicio = (ImageButton) findViewById(R.id.btn_ver_fecha_inicio);
        btn_ver_fecha_fin = (ImageButton) findViewById(R.id.btn_ver_fecha_final);

        btn_arrendar_alojamiento_submit = (Button) findViewById(R.id.btn_arrendar_alojamiento_submit);

        ly_fotos_alojamiento = (LinearLayout) findViewById(R.id.ly_fotos_alojamiento);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();

            Intent intent = getIntent();
        cargarAlojamiento((String) intent.getExtras().get("alojamiento"));

        btn_ver_fecha_inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esFechaInicio = true;
                obtenerFecha();
            }
        });

        btn_ver_fecha_fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esFechaInicio = false;
                obtenerFecha();
            }
        });

        cargarMenu();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }
        navigationView.setCheckedItem(R.id.nav_hospedajes);
    }

    private void cargarMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(HomeActivity.this, drawer, toolbar, R.string.nav_menu_abrir, R.string.nav_menu_cerrar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_hospedajes:
                intent = new Intent(HomeActivity.this, MapsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_perfil:
                intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_agregar_alojamiento:
                intent = new Intent(HomeActivity.this, AgregarAlojamientoActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_cerrar_sesion:
                mAuth.signOut();
                intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;

            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void cargarAlojamiento(String idAlojamiento){

        databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child(PATH_ALOJAMIENTOS).child(idAlojamiento)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Alojamiento alojamiento = dataSnapshot.getValue(Alojamiento.class);
                    //Log.d("Log_Home",alojamiento.getIdAlojamiento());

                    cargarFotos(alojamiento.getIdAlojamiento());

                    String si_no;

                    et_verTipoAlojamiento.setText(alojamiento.getTipo());
                    et_verPrecio.setText("$"+String.valueOf(alojamiento.getPrecio()));
                    et_verNumHuespedes.setText(String.valueOf(alojamiento.getHuespedes()));
                    et_verNumHabitaciones.setText(String.valueOf(alojamiento.getHabitaciones()));
                    et_verNumCamas.setText(String.valueOf(alojamiento.getCamas()));
                    et_verNumBanios.setText(String.valueOf(alojamiento.getBanos()));
                    si_no= (alojamiento.isWifi())?"Si":"No";
                    et_verWifi.setText(si_no);
                    si_no= (alojamiento.isElementosBasicos())?"Si":"No";
                    et_verElementosBasicos.setText(si_no);
                    si_no= (alojamiento.isTv())?"Si":"No";
                    et_verTV.setText(si_no);
                    si_no= (alojamiento.isArmario())?"Si":"No";
                    et_verArmario.setText(si_no);
                    si_no= (alojamiento.isEscritorio())?"Si":"No";
                    et_verEscritorio.setText(si_no);
                    si_no= (alojamiento.isDesayuno())?"Si":"No";
                    et_verDesayuno.setText(si_no);
                    si_no= (alojamiento.isAccesoCocina())?"Si":"No";
                    et_verAccesoCocina.setText(si_no);
                    si_no= (alojamiento.isBanoPrivado())?"Si":"No";
                    et_verBanioPrivado.setText(si_no);
                    et_descripcion_ver.setText("Descripción: "+alojamiento.getDescripcion());

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void cargarFotos(final String idAlojamiento){

        databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("fotos-alojamiento/").child(idAlojamiento)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot idFotoAlojamiento: dataSnapshot.getChildren()) {
                                //Log.d("Log_Home",idFotoAlojamiento.getValue(String.class));
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("fotos-alojamiento/" + idAlojamiento + "/" + idFotoAlojamiento.getValue(String.class) + ".jpg");
                                //Log.d("Log_Home","fotos-alojamiento/" + idAlojamiento + "/" + idFotoAlojamiento.getValue(String.class) + ".jpg");
                                storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        // Use the bytes to display the image
                                        imagenCargada = new BitmapDrawable(HomeActivity.this.getResources(), selectedImage);

                                        ImageView nuevaFoto = new ImageView(HomeActivity.this);
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        params.setMargins(dpToPx(8),dpToPx(8),dpToPx(8),dpToPx(8));
                                        params.height = dpToPx(100);
                                        params.width = dpToPx(100);

                                        nuevaFoto.setImageDrawable(imagenCargada);
                                        nuevaFoto.setLayoutParams(params);
                                        ly_fotos_alojamiento.addView(nuevaFoto);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    // Cargar fecha
    private void obtenerFecha() {
        DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //Esta variable lo que realiza es aumentar en uno el mes ya que comienza desde 0 = enero
                final int mesActual = month + 1;
                //Formateo el día obtenido: antepone el 0 si son menores de 10
                String diaFormateado = (dayOfMonth < 10) ? CERO + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                //Formateo el mes obtenido: antepone el 0 si son menores de 10
                String mesFormateado = (mesActual < 10) ? CERO + String.valueOf(mesActual) : String.valueOf(mesActual);
                //Muestro la fecha con el formato deseado

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);

                if (esFechaInicio) {
                    tv_ver_fecha_inicio.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    fechaInicio = cal.getTimeInMillis();
                } else {
                    tv_ver_fecha_fin.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    fechafin = cal.getTimeInMillis();
                }

            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
        }, anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();

    }

    public int dpToPx(int dp) {
        float density = HomeActivity.this.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }



}
