package com.gordos.earthbnb;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.gordos.earthbnb.modelo.Ubicacion;
import com.gordos.earthbnb.modelo.Usuario;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Variables menu
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // Constantes
    public static final String PATH_USUARIOS = "usuarios/";
    public static final String PATH_FOTOS_PERFIL = "fotos-perfil/";

    // Autenticación con Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    // Constantes
    private static final String TAG = "Resultado: ";

    // Variables de los elementos visuales
    private TextView tv_nombre;
    private TextView tv_apellido;
    private TextView tv_correo;
    private TextView tv_edad;
    private TextView tv_tipo_usuario;

    //    private TextView tv_img;
    private ImageView img_foto;

    // Variables locales
    private FirebaseUser usuario;
    private Uri photoUrl;

    // Imagen circular
    private Context mContext;
    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }

        mContext = getApplicationContext();
        mResources = getResources();

        // Inicialización de los elementos visuales
        tv_nombre = (TextView) findViewById(R.id.tv_nombre_perfil);
        tv_apellido = (TextView) findViewById(R.id.tv_apellido_perfil);
        tv_correo = (TextView) findViewById(R.id.tv_correo_perfil);
        tv_edad = (TextView) findViewById(R.id.tv_edad_perfil);
        tv_tipo_usuario = (TextView) findViewById(R.id.tv_tipo_usuario_perfil);

        img_foto = (ImageView) findViewById(R.id.img_usuario_foto_perfil);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Inicialización de variables locales
        usuario = mAuth.getCurrentUser();

        if(usuario != null){

            if(usuario.getPhotoUrl() != null){

                photoUrl = usuario.getPhotoUrl();
                Log.i(TAG, usuario.getUid() + ".jpg");
//                tv_img.setText(photoUrl.toString());

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(PATH_FOTOS_PERFIL + usuario.getUid() + ".jpg");
                storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        RoundedBitmapDrawable img_circular = createRoundedBitmapImageDrawableWithBorder(bitmap);

                        img_foto.setImageDrawable(img_circular);
                        // Use the bytes to display the image
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

            }

        }

        cargarMenu();
        cargarUsuario();
    }

    // Inflate de los elementos del menú
    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }
        navigationView.setCheckedItem(R.id.nav_perfil);
    }

    // Imagen circular
    private RoundedBitmapDrawable createRoundedBitmapImageDrawableWithBorder(Bitmap bitmap){
        int bitmapWidthImage = bitmap.getWidth();
        int bitmapHeightImage = bitmap.getHeight();
        int borderWidthHalfImage = 4;

        int bitmapRadiusImage = Math.min(bitmapWidthImage,bitmapHeightImage)/2;
        int bitmapSquareWidthImage = Math.min(bitmapWidthImage,bitmapHeightImage);
        int newBitmapSquareWidthImage = bitmapSquareWidthImage+borderWidthHalfImage;

        Bitmap roundedImageBitmap = Bitmap.createBitmap(newBitmapSquareWidthImage,newBitmapSquareWidthImage,Bitmap.Config.ARGB_8888);
        Canvas mcanvas = new Canvas(roundedImageBitmap);
        mcanvas.drawColor(Color.WHITE);
        int i = borderWidthHalfImage + bitmapSquareWidthImage - bitmapWidthImage;
        int j = borderWidthHalfImage + bitmapSquareWidthImage - bitmapHeightImage;

        mcanvas.drawBitmap(bitmap, i, j, null);

        Paint borderImagePaint = new Paint();
        borderImagePaint.setStyle(Paint.Style.STROKE);
        borderImagePaint.setStrokeWidth(borderWidthHalfImage*2);
        borderImagePaint.setColor(Color.WHITE);
        mcanvas.drawCircle(mcanvas.getWidth()/2, mcanvas.getWidth()/2, newBitmapSquareWidthImage/2, borderImagePaint);

        RoundedBitmapDrawable roundedImageBitmapDrawable = RoundedBitmapDrawableFactory.create(mResources,roundedImageBitmap);
        roundedImageBitmapDrawable.setCornerRadius(bitmapRadiusImage);
        roundedImageBitmapDrawable.setAntiAlias(true);
        return roundedImageBitmapDrawable;
    }

    // Menu

    private void cargarMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(ProfileActivity.this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(ProfileActivity.this, drawer, toolbar, R.string.nav_menu_abrir, R.string.nav_menu_cerrar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_hospedajes:
                intent = new Intent(ProfileActivity.this, MapsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_agregar_alojamiento:
                intent = new Intent(ProfileActivity.this, AgregarAlojamientoActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_cerrar_sesion:
                mAuth.signOut();
                intent = new Intent(ProfileActivity.this, LoginActivity.class);
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

    // Cargar usuario
    // Cargar JSON
    public void cargarUsuario() {
        databaseRef = database.getReference(PATH_USUARIOS + mAuth.getUid());
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuarioActual = dataSnapshot.getValue(Usuario.class);

                tv_nombre.setText(usuarioActual.getNombre());
                tv_apellido.setText(usuarioActual.getApellido());
                tv_correo.setText(usuarioActual.getCorreo());
                tv_tipo_usuario.setText(usuarioActual.getTipo());
                tv_edad.setText(usuarioActual.getEdad().toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error en la consulta", databaseError.toException());
            }
        });
    }
}
