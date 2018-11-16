package com.gordos.earthbnb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class NavHeader extends AppCompatActivity {

    private TextView tv_nombre_menu;
    private TextView tv_tipo_usuario_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_header);

        tv_nombre_menu = (TextView) findViewById(R.id.tv_nombre_menu);
        tv_nombre_menu.setText("Funciona");

    }
}
