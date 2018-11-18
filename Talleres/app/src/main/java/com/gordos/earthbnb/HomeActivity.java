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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Variables menu
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // Autenticación con Firebase
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            super.onBackPressed();
        }

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Toast.makeText(this, ((String) intent.getExtras().get("alojamiento")), Toast.LENGTH_SHORT).show();

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
            case R.id.nav_perfil:
                intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_agregar_alojamiento:
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



}
