package earthbandb.javeriana.edu.co.earthbandb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class ClientProfileActivity extends AppCompatActivity {
    ImageButton imgBtnAñadir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);
        imgBtnAñadir=(ImageButton) findViewById(R.id.imgBtnAñadir);

    }
}
