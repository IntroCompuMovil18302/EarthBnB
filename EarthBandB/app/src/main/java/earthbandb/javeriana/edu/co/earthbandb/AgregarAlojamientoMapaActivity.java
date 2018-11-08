package earthbandb.javeriana.edu.co.earthbandb;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgregarAlojamientoMapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Variables de permisos
    static final int PERMISO_GPS = 3;

    // Autenticación con Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    // Google Maps
    private GoogleMap mMap;

    // Variables de localización
    private FusedLocationProviderClient ubicacionCliente;
    private LocationRequest solicitudUbicacion;
    private LocationCallback callbackUbicacion;
    private Marker ubicacionActual;
    private Marker ubicacionDestino;
    private MarkerOptions ubicacionActualInfo;
    private MarkerOptions ubicacionDestinoInfo;
    private Geocoder mGeocoder;
    private Polyline ruta;

    // Variables de los elementos visuales
    private EditText et_ubicacion_busqueda;

    // Constantes
    private static final String TAG = "Resultado: ";
    private static final String PATH_UBICACIONES = "locations";
    static final int RADIO_DE_LA_TIERRA = 6371;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_alojamiento_mapa);

        // Inicialización de los elementos visuales
        et_ubicacion_busqueda = (EditText) findViewById(R.id.et_ubicacion_busqueda);

        // Inicialización de la ubicación
        ubicacionCliente = LocationServices.getFusedLocationProviderClient(this);
        solicitudUbicacion = crearSolicitudUbicacion();
        final Geocoder mGeocoder = new Geocoder(getBaseContext());

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Callback de ubicación
        callbackUbicacion = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.i("LOCATION", "Location update in the callback: " + location);
                if (location != null) {
                    // Actualiza el pin a la ubicación actual del usuario
                    LatLng ubicacionActualUsuario = new LatLng(location.getLatitude(), location.getLongitude());
                    ubicacionActual.remove();
                    ubicacionActualInfo = new MarkerOptions()
                            .position(ubicacionActualUsuario)
                            .title("Tú")
                            .snippet("estás aquí") //Texto de información
                            .alpha(0.5f).icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.home_location_marker));
                    ubicacionActual = mMap.addMarker(ubicacionActualInfo);

                    if (ubicacionDestino != null) {
                        LatLng centro = new LatLng(
                                (ubicacionActualInfo.getPosition().latitude + ubicacionDestinoInfo.getPosition().latitude) / 2,
                                (ubicacionActualInfo.getPosition().longitude + ubicacionDestinoInfo.getPosition().longitude) / 2
                        );
                        String url = getRequestUrl(ubicacionActualInfo.getPosition(), ubicacionDestinoInfo.getPosition());
                        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                        taskRequestDirections.execute(url);
//                        mMap.moveCamera(CameraUpdateFactory.newLatLng(centro));
//                        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionActualUsuario));
                    }

                    //Toast.makeText(MapsActivity.this,  String.valueOf(distance(location.getLatitude(),location.getLongitude(), 4.65, -74.05)), Toast.LENGTH_SHORT).show();
                }
            }
        };

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
                                    if (ubicacionDestino != null) {
                                        ubicacionDestino.remove();
                                    }
                                    ubicacionDestinoInfo = new MarkerOptions()
                                            .position(position)
                                            .title(et_ubicacion_busqueda.getText().toString())
                                            .snippet("A " + distance(
                                                    ubicacionActualInfo.getPosition().latitude,
                                                    ubicacionActualInfo.getPosition().longitude,
                                                    position.latitude,
                                                    position.longitude
                                            ) + " km de distancia") //Texto de información
                                            .alpha(0.5f).icon(BitmapDescriptorFactory
                                                    .defaultMarker(
                                                            BitmapDescriptorFactory.HUE_VIOLET
                                                    ));
                                    ubicacionDestino = mMap.addMarker(ubicacionDestinoInfo);

                                    // Pinta la ruta
                                    String url = getRequestUrl(ubicacionActualInfo.getPosition(), ubicacionDestinoInfo.getPosition());
                                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                                    taskRequestDirections.execute(url);

                                    LatLng centro = new LatLng(
                                            (ubicacionActualInfo.getPosition().latitude + ubicacionDestinoInfo.getPosition().latitude) / 2,
                                            (ubicacionActualInfo.getPosition().longitude + ubicacionDestinoInfo.getPosition().longitude) / 2
                                    );
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(centro));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                                }
                            } else {
                                Toast.makeText(AgregarAlojamientoMapaActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(AgregarAlojamientoMapaActivity.this, "La dirección esta vacía", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        //cargarUbicaciones();

    }

    @Override
    protected void onResume() {
        super.onResume();
        iniciarActualizacionesUbicacion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararActualizacionesUbicacion();
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

        // Se añade el estilo del mapa
        mMap.setMapStyle(MapStyleOptions
                .loadRawResourceStyle(this, R.raw.style_json));

        // Add a marker in Sydney and move the camera
        LatLng ciudad = new LatLng(4.65, -74.05);
        ubicacionActualInfo = new MarkerOptions()
                .position(ciudad)
                .title("BOGOTÁ")
                .snippet("Población: 8.081.000") //Texto de información
                .alpha(0.5f)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.home_location_marker));
        ubicacionActual = mMap.addMarker(ubicacionActualInfo);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ciudad));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(true);
        }
    }

    // Configuración de lozalización

    /**
     * Finaliza la actualización de la posición de la ubicación del usuario
     */
    private void pararActualizacionesUbicacion() {
        //Verificación de permiso!!
        ubicacionCliente.removeLocationUpdates(callbackUbicacion);
    }

    /**
     * Inicia la actualización de la posición de la ubicación del usuario
     */
    private void iniciarActualizacionesUbicacion() {
        //Verificación de permiso!!
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ubicacionCliente.requestLocationUpdates(solicitudUbicacion, callbackUbicacion, null);
        }
    }

    /**
     * Solicitta la ubicación cada cierto tiempo
     *
     * @return LocationReques : Ubicación actual del usuario
     */
    protected LocationRequest crearSolicitudUbicacion() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //tasa de refresco en milisegundos
        mLocationRequest.setFastestInterval(5000); //máxima tasa de refresco
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

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
            LocationSettingsRequest.Builder builder = new
                    LocationSettingsRequest.Builder().addLocationRequest(solicitudUbicacion);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    iniciarActualizacionesUbicacion(); //Todas las condiciones para recibir localizaciones
                }
            });

            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                            try {// Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(AgregarAlojamientoMapaActivity.this, PERMISO_GPS);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. No way to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    // Inflate de los elementos del menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem elemento) {
        int elementoPresionado = elemento.getItemId();
        if (elementoPresionado == R.id.menuCerrarSesion) {
            mAuth.signOut();
            Intent intent = new Intent(AgregarAlojamientoMapaActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (elementoPresionado == R.id.menuPerfil) {
            Intent intent = new Intent(AgregarAlojamientoMapaActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(elemento);
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
//                    mMap.setMyLocationEnabled(true);
                    iniciarActualizacionesUbicacion();
//                    Log.i("Mensaje:", "Permiso concedido");
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
                    iniciarActualizacionesUbicacion(); //Se encendió la localización!!!
                } else {
                    Toast.makeText(this,
                            "Sin acceso a localización, hardware deshabilitado!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    // Dibujar ruta
    private String getRequestUrl(LatLng origen, LatLng destino) {

        String str_origen = "origin=" + origen.latitude + "," + origen.longitude;
        String str_desttino = "destination=" + destino.latitude + "," + destino.longitude;
        String str_sensor = "sensor=false";
        String str_mode = "mode=driving";
        String str_param = str_origen + "&" + str_desttino + "&" + str_sensor + "&" + str_mode;
        String str_salida = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + str_salida + "?" + str_param + "&key=AIzaSyBhHTgsUynwhanscYcaDWNTpGQSbdZiAhI";

        return url;

    }

    private String requestDirection(String urlSolicitada) throws IOException {

        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlSolicitada);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }

        return responseString;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String respuesta = "";
            try {
                respuesta = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(5);
                polylineOptions.color(Color.MAGENTA);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions != null) {
                if (ruta != null) {
                    ruta.remove();
                }
                ruta = mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Cargar JSON
    public void cargarUbicaciones() {
        databaseRef = database.getReference(PATH_UBICACIONES);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Ubicacion nuevaUbicacion = singleSnapshot.getValue(Ubicacion.class);
                    Log.i(TAG, nuevaUbicacion.getName());

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(nuevaUbicacion.getLatitude(), nuevaUbicacion.getLongitude()))
                            .title(nuevaUbicacion.getName())
                            .alpha(0.5f).icon(BitmapDescriptorFactory
                                    .defaultMarker(
                                            BitmapDescriptorFactory.HUE_ORANGE
                                    )));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error en la consulta", databaseError.toException());
            }
        });
    }
}
