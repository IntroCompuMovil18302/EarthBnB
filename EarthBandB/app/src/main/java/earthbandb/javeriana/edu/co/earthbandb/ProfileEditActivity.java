package earthbandb.javeriana.edu.co.earthbandb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfileEditActivity extends AppCompatActivity {
    EditText TextName;
    TextView TextEmail;
    ImageButton ImgBtnUser;
    String idUser;
    Uri selectedImage;
    private static final int SELECT_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        String nombre=getIntent().getExtras().getString("nombre");
        String correo=getIntent().getExtras().getString("correo");
        idUser=getIntent().getExtras().getString("id");
        TextName=(EditText)findViewById(R.id.nombreEditText);
        TextEmail=(TextView)findViewById(R.id.emailTextView);
        ImgBtnUser=(ImageButton)findViewById(R.id.imgBtnUser);
        TextEmail.setText(correo);
        TextName.setText(nombre);

        ImgBtnUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirGaleria(v);
            }
        });

    }
    public void abrirGaleria(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),
                SELECT_FILE);
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Uri selectedImageUri = null;


        String filePath = null;
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath=selectedImage.getPath();
                    if (requestCode == SELECT_FILE) {

                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                imageStream = getContentResolver().openInputStream(
                                        selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                            // Ponemos nuestro bitmap en un ImageView que tengamos en la vista
                            ImgBtnUser.setImageBitmap(bmp);

                        }
                    }
                }
                break;
        }
    }
}
