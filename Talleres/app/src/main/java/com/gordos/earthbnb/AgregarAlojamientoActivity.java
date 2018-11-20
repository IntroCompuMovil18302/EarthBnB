package com.gordos.earthbnb;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gordos.earthbnb.modelo.Alojamiento;
import com.gordos.earthbnb.modelo.AlojamientoUsuario;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AgregarAlojamientoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Constantes
    private static final int PLACE_PICKER_REQUEST = 5; // Variable para la validación de escoger una ubicación
    private static final String PATH_ALOJAMIENTOS = "alojamientos/";
    private static final String PATH_ALOJAMIENTOS_USUARIO = "alojamientos-usuarios/";
    private static final String PATH_FOTOS_ALOJAMIENTO = "fotos-alojamiento/";

    // Variables de permisos
    static final int PERMISO_ALMACENAMIENTO = 2;
    static final int PERMISO_GPS = 3;

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

    // Variables de los elementos visuales
    private TextView tv_ubicacion_alojamiento;
    private TextView tv_fecha_inicio;
    private TextView tv_fecha_fin;
    private TextView tv_agregar_fotos;
    private ImageButton btn_ubicacion_alojamiento;
    private ImageButton btn_agregar_foto;
    private ImageButton btn_fecha_inicio;
    private ImageButton btn_fecha_fin;
    private Spinner sp_tipo_alojamiento;
    private EditText et_huespedes;
    private EditText et_habitaciones;
    private EditText et_camas;
    private EditText et_banos;
    private EditText et_descripcion;
    private Switch sw_bano_privado;
    private Switch sw_elementos_basicos;
    private Switch sw_wifi;
    private Switch sw_tv;
    private Switch sw_armario;
    private Switch sw_escritorio;
    private Switch sw_desayuno;
    private Switch sw_acceso_cocina;
    private Button btn_agregar_alojamiento_submit;
    LinearLayout ly_fotos_agregadas;

    // Variables locales
    private LatLng ubicaciónAlojamientoLatLng;
    private int numeroImagenSeleccionada;
    private BitmapDrawable imagenCargada;
    private boolean esFechaInicio;
    private long fechaInicio;
    private long fechafin;
    private List<BitmapDrawable> imagenesCargadas;

    // Constantes calendario
    private static final String CERO = "0";
    private static final String BARRA = "/";

    // Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    // Variables para obtener la fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_alojamiento);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }

        // Inicialización de variables locales
        ubicaciónAlojamientoLatLng = null;
        esFechaInicio = true;
        fechaInicio = -1;
        fechafin = -1;
        imagenesCargadas = new ArrayList<BitmapDrawable>();

        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance();


        // Inicialización de variables de elementos visuales
        tv_ubicacion_alojamiento = (TextView) findViewById(R.id.tv_ubicacion_alojamiento);
        tv_fecha_inicio = (TextView) findViewById(R.id.tv_fecha_inicio);
        tv_fecha_fin = (TextView) findViewById(R.id.tv_fecha_fin);
        tv_agregar_fotos = (TextView) findViewById(R.id.tv_agregar_fotos);

        et_huespedes = (EditText) findViewById(R.id.et_huespedes);
        et_habitaciones = (EditText) findViewById(R.id.et_habitaciones);
        et_camas = (EditText) findViewById(R.id.et_camas);
        et_banos = (EditText) findViewById(R.id.et_banos);
        et_descripcion = (EditText) findViewById(R.id.et_descripcion);

        sw_bano_privado = (Switch) findViewById(R.id.sw_bano_privado);
        sw_elementos_basicos = (Switch) findViewById(R.id.sw_elementos_basicos);
        sw_wifi = (Switch) findViewById(R.id.sw_wifi);
        sw_tv = (Switch) findViewById(R.id.sw_tv);
        sw_armario = (Switch) findViewById(R.id.sw_armario);
        sw_escritorio = (Switch) findViewById(R.id.sw_escritorio);
        sw_desayuno = (Switch) findViewById(R.id.sw_desayuno);
        sw_acceso_cocina = (Switch) findViewById(R.id.sw_cocina);

        btn_ubicacion_alojamiento = (ImageButton) findViewById(R.id.btn_agregar_alojamiento);
        btn_fecha_inicio = (ImageButton) findViewById(R.id.btn_agregar_fecha_inicio);
        btn_fecha_fin = (ImageButton) findViewById(R.id.btn_agregar_fecha_final);
        btn_agregar_foto = (ImageButton) findViewById(R.id.btn_agregar_foto);

        sp_tipo_alojamiento = (Spinner) findViewById(R.id.sp_tipo_alojamiento);

        btn_agregar_alojamiento_submit = (Button) findViewById(R.id.btn_agregar_alojamiento_submit);

        ly_fotos_agregadas = (LinearLayout) findViewById(R.id.ly_fotos_agregadas);

        ArrayAdapter adaptador = ArrayAdapter.createFromResource(this, R.array.tipo_array, R.layout.spinner_personalizado);
        adaptador.setDropDownViewResource(R.layout.spinner_desplegable_personalizado);
        sp_tipo_alojamiento.setAdapter(adaptador);

        // Manejo de eventos de botones
        btn_ubicacion_alojamiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Toast.makeText(AgregarAlojamientoActivity.this, "Elija su ubicación", Toast.LENGTH_SHORT).show();

                try {
                    Intent intent = builder.build(AgregarAlojamientoActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_agregar_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarPermisoAlmacenamiento();
                if (ActivityCompat.checkSelfPermission(AgregarAlojamientoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(AgregarAlojamientoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent seleccionarImagen = new Intent(Intent.ACTION_PICK);
                    seleccionarImagen.setType("image/*");
                    numeroImagenSeleccionada = 1;
                    startActivityForResult(seleccionarImagen, PERMISO_ALMACENAMIENTO);
                }
            }
        });

        btn_fecha_inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esFechaInicio = true;
                obtenerFecha();
            }
        });

        btn_fecha_fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esFechaInicio = false;
                obtenerFecha();
            }
        });

        btn_agregar_alojamiento_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarAlojamiento();
            }
        });

        solicitarPermiso();
        cargarMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }
        navigationView.setCheckedItem(R.id.nav_agregar_alojamiento);
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
                    tv_fecha_inicio.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    fechaInicio = cal.getTimeInMillis();
                } else {
                    tv_fecha_fin.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    fechafin = cal.getTimeInMillis();
                }

            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
        }, anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();

    }

    /**
     * Valida si los campos de autenticación están vacíos o bien diligenciados.
     *
     * @return boolean: Si el formato ha sido llenado correctamente o no.
     */
    private boolean validateForm() {
        boolean valido = true;

        if (TextUtils.isEmpty(et_huespedes.getText().toString()) || TextUtils.isEmpty(et_habitaciones.getText().toString()) || TextUtils.isEmpty(et_camas.getText().toString()) || TextUtils.isEmpty(et_banos.getText().toString())) {
            et_huespedes.setError("¡Este campo es requerido!");
            et_camas.setError("¡Este campo es requerido!");
            et_habitaciones.setError("¡Este campo es requerido!");
            et_banos.setError("¡Este campo es requerido!");
            return false;
        }

        // Validación seleccion de ubicación
        if (ubicaciónAlojamientoLatLng == null) {
            tv_ubicacion_alojamiento.setError("¡Este campo es requerido!");
            tv_ubicacion_alojamiento.setText("¡Se debe seleccionar una ubicaicón!");
            valido = false;
        } else {
            tv_ubicacion_alojamiento.setError(null);
        }

        // Validación número de huéspedes
        int huespedes = Integer.parseInt(et_huespedes.getText().toString());
        if (huespedes == 0) {
            et_huespedes.setError("¡Este campo no puede ser 0!");
            valido = false;
        } else {
            et_huespedes.setError(null);
        }

        // Validación número de huéspedes
        int habitaciones = Integer.parseInt(et_habitaciones.getText().toString());
        if (habitaciones == 0) {
            et_habitaciones.setError("¡Este campo no puede ser 0!");
            valido = false;
        } else {
            et_habitaciones.setError(null);
        }

        // Validación número de camas
        int camas = Integer.parseInt(et_camas.getText().toString());
        if (camas == 0) {
            et_camas.setError("¡Este campo no puede ser 0!");
            valido = false;
        } else {
            et_camas.setError(null);
        }

        // Validación número de baños
        int banos = Integer.parseInt(et_banos.getText().toString());
        if (habitaciones == 0) {
            et_banos.setError("¡Este campo no puede ser 0!");
            valido = false;
        } else {
            et_banos.setError(null);
        }

        // Validación de fotos
        if (imagenesCargadas.size() < 4) {
            tv_agregar_fotos.setError("¡Se debe agregar por lo menos 4 fotos del alojamiento!");
            valido = false;
        } else {
            tv_agregar_fotos.setError(null);
        }

        // Validación de la descripción
        String descripcion = et_descripcion.getText().toString();
        if (TextUtils.isEmpty(descripcion)) {
            et_descripcion.setError("¡Se debe agregar una descripción del alojamiento!");
            valido = false;
        } else {
            et_descripcion.setError(null);
        }

        // Validación fecha de inicio
        if (fechaInicio == -1) {
            tv_fecha_inicio.setError("¡Se necesita una fecha de inicio de disponibilidad!");
            tv_fecha_inicio.setText("¡Se necesita una fecha de inicio de disponibilidad!");
            valido = false;
        } else if (fechaInicio < c.getTimeInMillis()) {
            tv_fecha_inicio.setError("¡La fecha ya pasó!");
            tv_fecha_inicio.setText("¡La fecha ya pasó!");
            valido = false;
        } else {
            tv_fecha_inicio.setError(null);
        }

        // Validación fecha de fin
        if (fechafin == -1) {
            tv_fecha_fin.setError("¡Se necesita una fecha de fin de disponibilidad!");
            tv_fecha_fin.setText("¡Se necesita una fecha de fin de disponibilidad!");
            valido = false;
        } else if (fechafin < fechaInicio) {
            tv_fecha_fin.setError("¡La fecha de fin debe ser después de la fecha de inicio!");
            tv_fecha_fin.setText("¡La fecha de fin debe ser después de la fecha de inicio!");
            valido = false;
        } else {
            tv_fecha_fin.setError(null);
        }

        return valido;
    }

    private void agregarAlojamiento() {

        if (validateForm()) {
            String key = "";
            Alojamiento nuevoAlojamiento = new Alojamiento();
            AlojamientoUsuario nuevoAlojamientoUsuario = new AlojamientoUsuario();

            databaseRef = database.getReference("alojamientos");
            key = databaseRef.push().getKey();

            nuevoAlojamientoUsuario.setIdAlojamiento(key);

            if (imagenesCargadas.size() >= 4) {

                for (BitmapDrawable imagen: imagenesCargadas) {
                    databaseRef = database.getReference("fotos-alojamiento");
                    String fotoKey = databaseRef.push().getKey();

                    imageRef = storageRef.child(PATH_FOTOS_ALOJAMIENTO + key + "/" + fotoKey + ".jpg");
                    Bitmap bitmap = imagen.getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = imageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Imagen", "Falla");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i("Imagen", "NO falla");
                        }
                    });
                    databaseRef = database.getReference(PATH_FOTOS_ALOJAMIENTO + "/" + key);
                    databaseRef.setValue(fotoKey);

                }
                nuevoAlojamiento.setUrlFotos(key);
            }

            nuevoAlojamiento.setLatitude(ubicaciónAlojamientoLatLng.latitude);
            nuevoAlojamiento.setLongitude(ubicaciónAlojamientoLatLng.longitude);
            nuevoAlojamiento.setTipo(sp_tipo_alojamiento.getSelectedItem().toString());
            nuevoAlojamiento.setHuespedes(Integer.parseInt(et_huespedes.getText().toString()));
            nuevoAlojamiento.setHabitaciones(Integer.parseInt(et_habitaciones.getText().toString()));
            nuevoAlojamiento.setCamas(Integer.parseInt(et_camas.getText().toString()));
            nuevoAlojamiento.setBanos(Integer.parseInt(et_banos.getText().toString()));
            nuevoAlojamiento.setBanoPrivado(sw_bano_privado.isChecked());
            nuevoAlojamiento.setElementosBasicos(sw_elementos_basicos.isChecked());
            nuevoAlojamiento.setWifi(sw_wifi.isChecked());
            nuevoAlojamiento.setTv(sw_tv.isChecked());
            nuevoAlojamiento.setArmario(sw_armario.isChecked());
            nuevoAlojamiento.setEscritorio(sw_escritorio.isChecked());
            nuevoAlojamiento.setDesayuno(sw_desayuno.isChecked());
            nuevoAlojamiento.setAccesoCocina(sw_acceso_cocina.isChecked());
            nuevoAlojamiento.setIdUsuario(mAuth.getUid());
            nuevoAlojamiento.setFechaInicio(fechaInicio);
            nuevoAlojamiento.setFechaFin(fechafin);
            nuevoAlojamiento.setDescripcion(et_descripcion.getText().toString());
            nuevoAlojamiento.setIdAlojamiento(key);
            nuevoAlojamiento.setCalificacion(-1);

            databaseRef = database.getReference(PATH_ALOJAMIENTOS + key);
            databaseRef.setValue(nuevoAlojamiento);

            databaseRef = database.getReference(PATH_ALOJAMIENTOS_USUARIO + mAuth.getUid());
            key = databaseRef.push().getKey();

            databaseRef = database.getReference(PATH_ALOJAMIENTOS_USUARIO + mAuth.getUid() + "/" + key);
            databaseRef.setValue(nuevoAlojamientoUsuario);

            Toast.makeText(this, "¡Su alojamiento ha sido agregado!", Toast.LENGTH_SHORT).show();
            super.onBackPressed();

        }

    }

    // Menu
    private void cargarMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(AgregarAlojamientoActivity.this, drawer, toolbar, R.string.nav_menu_abrir, R.string.nav_menu_cerrar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.nav_hospedajes:
                intent = new Intent(AgregarAlojamientoActivity.this, MapsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_perfil:
                intent = new Intent(AgregarAlojamientoActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_agregar_alojamiento:
                break;

            case R.id.nav_cerrar_sesion:
                mAuth.signOut();
                intent = new Intent(AgregarAlojamientoActivity.this, LoginActivity.class);
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

    // Solicitar permisos

    /**
     * Solicita acceso a la ubicación
     */
    private void solicitarPermiso() {

        int validadorPermiso = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (validadorPermiso != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_GPS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Es necesario tener acceso a la ubicación", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_GPS);
            }
        } else {
            Log.i("Mensaje:", "Se le dio el permiso");
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
            case PERMISO_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Log.i("Mensaje:", "Permiso concedido");
                } else {
                    // permission denied, disable functionality that depends on this permission.
                    Log.i("Mensaje:", "Permiso denegado");
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
        }
    }

    // Validación de ubicación
    // Re-escritura del metodo onActivity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            // Verificación de acceso a la ubicación
            case PERMISO_GPS:
                if (resultCode == RESULT_OK) {
                    Log.i("Mensaje:", "Ubicación encendida");
                } else {
                    Toast.makeText(this, "Sin acceso a localización, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
                break;

            // Verificación de que se obtuvo un resultado del Place Picker
            case PLACE_PICKER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    tv_ubicacion_alojamiento.setText(place.getAddress());
                    Toast.makeText(this, place.getAddress(), Toast.LENGTH_SHORT).show();
                    ubicaciónAlojamientoLatLng = place.getLatLng();
                }
                break;

            case PERMISO_ALMACENAMIENTO:
                if (resultCode == RESULT_OK) {
//                    Toast.makeText(this, "Se ha autorizado el uso del almacenamiento", Toast.LENGTH_LONG).show();
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imagenCargada = new BitmapDrawable(AgregarAlojamientoActivity.this.getResources(), selectedImage);
                        imagenesCargadas.add(imagenCargada);

                        ImageView nuevaFoto = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(dpToPx(8),dpToPx(8),dpToPx(8),dpToPx(8));
                        params.height = dpToPx(100);
                        params.width = dpToPx(100);

                        nuevaFoto.setImageDrawable(imagenCargada);
                        nuevaFoto.setLayoutParams(params);
                        ly_fotos_agregadas.addView(nuevaFoto);


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public int dpToPx(int dp) {
        float density = AgregarAlojamientoActivity.this.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}
