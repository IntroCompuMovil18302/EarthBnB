package com.gordos.earthbnb;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gordos.earthbnb.modelo.Alojamiento;
import com.gordos.earthbnb.modelo.Ubicacion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

// LINK PIN HOME: https://www.flaticon.com/free-icon/home-location-marker_65433

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    // Variables menu
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // Variables de permisos
    static final int PERMISO_GPS = 3;

    // Autenticación con Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    // Google Maps
    private GoogleMap mMap;
    private CircleOptions circleOptions;
    private Circle circle;

    // Variables de localización
    private Marker ubicacionActual;
    private MarkerOptions ubicacionActualInfo;

    // Variables de los elementos visuales
    private EditText et_ubicacion_busqueda;
    private TextView tv_desde;
    private TextView tv_hasta;
    private ImageButton btn_fecha_desde;
    private ImageButton btn_fecha_hasta;
    private Button btn_filtrar_fecha;

    // Constantes
    private static final String TAG = "Resultado: ";
    private static final String PATH_ALOJAMIENTOS = "alojamientos/";
    static final int RADIO_DE_LA_TIERRA = 6371;
    private static final String CERO = "0";
    private static final String BARRA = "/";

    // Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    // Variables para obtener la fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);

    // Variables locales
    private boolean esFechaInicio;
    private long fechaInicio;
    private long fechafin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }

        esFechaInicio = true;
        fechaInicio = -1;
        fechafin = -1;

        // Inicialización de los elementos visuales
        et_ubicacion_busqueda = (EditText) findViewById(R.id.et_ubicacion_busqueda);
        tv_desde = (TextView) findViewById(R.id.tv_desde);
        tv_hasta = (TextView) findViewById(R.id.tv_hasta);
        btn_fecha_desde = (ImageButton) findViewById(R.id.btn_fecha_desde);
        btn_fecha_hasta = (ImageButton) findViewById(R.id.btn_fecha_hasta);
        btn_filtrar_fecha = (Button) findViewById(R.id.btn_filtrar_fecha);

        btn_fecha_desde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esFechaInicio = true;
                obtenerFecha();
            }
        });

        btn_fecha_hasta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esFechaInicio = false;
                obtenerFecha();
            }
        });

        btn_filtrar_fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarUbicacionesFiltros();
            }
        });

        // Inicialización de la ubicación
        final Geocoder mGeocoder = new Geocoder(getBaseContext());

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Petición de permisos de localización
        solicitarPermiso();
        solicitarUbicacion();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        et_ubicacion_busqueda.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    String addressString = et_ubicacion_busqueda.getText().toString();
                    if (!addressString.isEmpty()) {
                        try {
                            List<Address> addresses = mGeocoder.getFromLocationName(addressString, 2);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address addressResult = addresses.get(0);
                                LatLng position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                                if (mMap != null) {
                                    //Agregar Marcador al mapa
                                    if (ubicacionActual != null) {
                                        ubicacionActual.remove();
                                    }

                                    mMap.clear();

                                    ubicacionActualInfo = new MarkerOptions()
                                            .position(position)
                                            .title(et_ubicacion_busqueda.getText().toString())
                                            .snippet("Estás buscando sitios cerca de aquí") //Texto de información
                                            .alpha(1f).icon(BitmapDescriptorFactory.fromResource(R.drawable.human_location));
                                    ubicacionActual = mMap.addMarker(ubicacionActualInfo);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12));

                                    circle.remove();

                                    circleOptions = new CircleOptions()
                                            .center(position)
                                            .radius(2000) //metros
                                            .strokeWidth(3)
                                            .strokeColor(Color.BLACK)
                                            .fillColor(Color.argb(32, 0, 0, 0))
                                            .clickable(true);

                                    circle = mMap.addCircle(circleOptions);

                                    cargarUbicaciones();
                                }
                            } else {
                                Toast.makeText(MapsActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "La dirección esta vacía", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        cargarMenu();
        cargarUbicaciones();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }
        navigationView.setCheckedItem(R.id.nav_hospedajes);
        if (mMap != null) {
            mMap.clear();
            obtenerUbicacionActual();
            cargarUbicaciones();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            moveTaskToBack(true);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(!marker.equals(ubicacionActual)){
                    Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
                    intent.putExtra("alojamiento", ((Alojamiento) marker.getTag()).getIdAlojamiento());
                    startActivity(intent);
                }
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                ubicacionActual.remove();
                mMap.clear();

                ubicacionActualInfo = new MarkerOptions()
                        .position(latLng)
                        .title("Tú")
                        .snippet("Estás aquí") //Texto de información
                        .alpha(1f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.human_location));

                ubicacionActual = mMap.addMarker(ubicacionActualInfo);

                circle.remove();

                circleOptions = new CircleOptions()
                        .center(latLng)
                        .radius(2000) //metros
                        .strokeWidth(3)
                        .strokeColor(Color.BLACK)
                        .fillColor(Color.argb(32, 0, 0, 0))
                        .clickable(true);

                circle = mMap.addCircle(circleOptions);

                cargarUbicaciones();
            }
        });

        // Se añade el estilo del mapa
        mMap.setMapStyle(MapStyleOptions
                .loadRawResourceStyle(this, R.raw.style_json));

        // Add a marker in Sydney and move the camera
        LatLng ciudad = new LatLng(4.65, -74.05);
        ubicacionActualInfo = new MarkerOptions()
                .position(ciudad)
                .title("BOGOTÁ")
                .snippet("Población: 8.081.000") //Texto de información
                .alpha(1f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.human_location));
        ubicacionActual = mMap.addMarker(ubicacionActualInfo);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ciudad));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        circleOptions = new CircleOptions()
                .center(ciudad)
                .radius(2000) //metros
                .strokeWidth(3)
                .strokeColor(Color.BLACK)
                .fillColor(Color.argb(32, 0, 0, 0))
                .clickable(true);

        circle = mMap.addCircle(circleOptions);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setPadding(0, 0, 8, 8);
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng posicionActual = new LatLng(location.getLatitude(), location.getLongitude());

                                ubicacionActual.remove();

                                ubicacionActualInfo = new MarkerOptions()
                                        .position(posicionActual)
                                        .title("Tú")
                                        .snippet("Estás aquí") //Texto de información
                                        .alpha(1f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.human_location));

                                ubicacionActual = mMap.addMarker(ubicacionActualInfo);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicionActual));
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(12));

                                circle.remove();

                                circleOptions = new CircleOptions()
                                        .center(posicionActual)
                                        .radius(2000) //metros
                                        .strokeWidth(3)
                                        .strokeColor(Color.BLACK)
                                        .fillColor(Color.argb(32, 0, 0, 0))
                                        .clickable(true);

                                circle = mMap.addCircle(circleOptions);

                            }
                        }
                    });
        }
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
                    tv_desde.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    cal.add(Calendar.DATE, -1);
                    fechaInicio = cal.getTimeInMillis();
                } else {
                    tv_hasta.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    cal.add(Calendar.DATE, 1);
                    fechafin = cal.getTimeInMillis();
                }

            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
        }, anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();

    }

    // Configuración de lozalización

    /**
     * Determina la distancia del usuario con respecto a otro punto
     *
     * @param lat1  : Latitud actual del usuario
     * @param long1 : Longitud actual del usuario
     * @param lat2  : Latitud del punto al que se desea llegar
     * @param long2 : Longitud del punto al que se desea llegar
     * @return double : Distancia entre los puntos en km
     */
    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIO_DE_LA_TIERRA * c;
        return Math.round(result * 100.0) / 100.0;
    }

    /**
     * Solicita la ubicación actual del usuario
     */
    private void solicitarUbicacion() {

        Log.i("ERROR:", "ACÁ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("ERROR:", "NO TIENE PERMISO");
        } else {
            obtenerUbicacionActual();
        }
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setPadding(0, 0, 8, 8);
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    LatLng posicionActual = new LatLng(location.getLatitude(), location.getLongitude());

                                    ubicacionActual.remove();

                                    ubicacionActualInfo = new MarkerOptions()
                                            .position(posicionActual)
                                            .title("Tú")
                                            .snippet("Estás aquí") //Texto de información
                                            .alpha(1f)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.human_location));

                                    ubicacionActual = mMap.addMarker(ubicacionActualInfo);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicionActual));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12));

                                    circle.remove();

                                    circleOptions = new CircleOptions()
                                            .center(posicionActual)
                                            .radius(2000) //metros
                                            .strokeWidth(3)
                                            .strokeColor(Color.BLACK)
                                            .fillColor(Color.argb(32, 0, 0, 0))
                                            .clickable(true);

                                    circle = mMap.addCircle(circleOptions);

                                }
                            }
                        });
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISO_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    obtenerUbicacionActual();

                } else {
                    // permission denied, disable functionality that depends on this permission.
                    Log.i("Mensaje:", "Permiso denegado");
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
            case PERMISO_GPS: {
                if (resultCode == RESULT_OK) {
                    obtenerUbicacionActual();
                } else {
                    Toast.makeText(this,
                            "Sin acceso a localización, hardware deshabilitado!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    // Cargar JSON
    public void cargarUbicaciones() {
        databaseRef = database.getReference(PATH_ALOJAMIENTOS);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Alojamiento nuevoAlojamiento = singleSnapshot.getValue(Alojamiento.class);
                    Log.i(TAG, nuevoAlojamiento.getTipo());

                    double distancia = distance(ubicacionActual.getPosition().latitude, ubicacionActual.getPosition().longitude, nuevoAlojamiento.getLatitude(), nuevoAlojamiento.getLongitude());
                    Log.i(TAG, "" + distancia);
                    if (distancia <= 2) {
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(nuevoAlojamiento.getLatitude(), nuevoAlojamiento.getLongitude()))
                                .title(nuevoAlojamiento.getTipo())
                                .snippet(nuevoAlojamiento.getDescripcion())
                                .alpha(1f).icon(BitmapDescriptorFactory.fromResource(R.drawable.house_location)));
                        m.setTag(nuevoAlojamiento);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error en la consulta", databaseError.toException());
            }
        });
    }

    // Cargar JSON
    public void cargarUbicacionesFiltros() {

        if (fechafin == -1 || fechaInicio == -1) {
            Toast.makeText(this, "Se debe elegir fecha de inicio y fecha final", Toast.LENGTH_SHORT).show();
        } else if(fechafin < fechaInicio) {
            Toast.makeText(this, "la fecha final debe ser después de la fecha inicial", Toast.LENGTH_SHORT).show();
        } else {
            mMap.clear();

            ubicacionActualInfo = new MarkerOptions()
                    .position(ubicacionActual.getPosition())
                    .title("Tú")
                    .snippet("Estás aquí") //Texto de información
                    .alpha(1f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.human_location));

            ubicacionActual = mMap.addMarker(ubicacionActualInfo);

            circle.remove();

            circleOptions = new CircleOptions()
                    .center(ubicacionActual.getPosition())
                    .radius(2000) //metros
                    .strokeWidth(3)
                    .strokeColor(Color.BLACK)
                    .fillColor(Color.argb(32, 0, 0, 0))
                    .clickable(true);

            circle = mMap.addCircle(circleOptions);


            databaseRef = database.getReference(PATH_ALOJAMIENTOS);
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Alojamiento nuevoAlojamiento = singleSnapshot.getValue(Alojamiento.class);
                        Log.i(TAG, nuevoAlojamiento.getTipo());

                        double distancia = distance(ubicacionActual.getPosition().latitude, ubicacionActual.getPosition().longitude, nuevoAlojamiento.getLatitude(), nuevoAlojamiento.getLongitude());
                        Log.i(TAG, "" + distancia);
                        if (distancia <= 2) {
                            if((fechaInicio >= nuevoAlojamiento.getFechaInicio() && fechaInicio <= nuevoAlojamiento.getFechaFin()) && (fechafin >= nuevoAlojamiento.getFechaInicio() && fechafin <= nuevoAlojamiento.getFechaFin())){
                                Marker m = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(nuevoAlojamiento.getLatitude(), nuevoAlojamiento.getLongitude()))
                                        .title(nuevoAlojamiento.getTipo())
                                        .snippet(nuevoAlojamiento.getDescripcion())
                                        .alpha(1f).icon(BitmapDescriptorFactory.fromResource(R.drawable.house_location)));
                                m.setTag(nuevoAlojamiento);
                            }

                        }
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Error en la consulta", databaseError.toException());
                }
            });
        }

    }

    // Menu
    private void cargarMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MapsActivity.this, drawer, toolbar, R.string.nav_menu_abrir, R.string.nav_menu_cerrar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.nav_hospedajes:
                break;

            case R.id.nav_perfil:
                intent = new Intent(MapsActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_agregar_alojamiento:
                intent = new Intent(MapsActivity.this, AgregarAlojamientoActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_cerrar_sesion:
                mAuth.signOut();
                intent = new Intent(MapsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;

            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

}
