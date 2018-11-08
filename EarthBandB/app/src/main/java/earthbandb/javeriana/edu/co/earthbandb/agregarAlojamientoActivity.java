package earthbandb.javeriana.edu.co.earthbandb;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class agregarAlojamientoActivity extends AppCompatActivity implements View.OnClickListener {

    Button agregar;
    EditText valor;
    EditText descripcion;
    Spinner spinCiudad;
    Spinner spinHuesp;
    Spinner spinTipo;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    DatabaseReference myRef;
    FirebaseDatabase database;
    ImageButton imagenCarg;
    ImageButton imagenCarg2;
    ImageButton imagenCarg3;
    ImageButton imagenCarg4;
    ImageButton btnAgregarAlojamientoMapa;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri selectedImage;
    Uri selectedImage2;
    Uri selectedImage3;
    Uri selectedImage4;
    LatLng ubicacionAlojamiento;
    private static final int SELECT_FILE = 1;
    private static final String CERO = "0";
    private static final String BARRA = "/";

    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Variables para obtener la fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    EditText etFecha;
    ImageButton ibObtenerFecha;

    private static final int PLACE_PICKER_REQUEST = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agregar_alojamiento);
        agregar = (Button) findViewById(R.id.btnAgregarAlojamiento);
        valor = (EditText) findViewById(R.id.ValoreditText);
        descripcion = (EditText) findViewById(R.id.CiudadtextView);
        spinTipo = (Spinner) findViewById(R.id.spinner);
        spinCiudad = (Spinner) findViewById(R.id.Ciudadspinner);
        spinHuesp = (Spinner) findViewById(R.id.cantSpinner);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance();
        imagenCarg = (ImageButton) findViewById(R.id.imagenCargada);
        btnAgregarAlojamientoMapa = (ImageButton) findViewById(R.id.img_btn_map);
        imagenCarg2=(ImageButton)findViewById(R.id.imagenCargada2);
        imagenCarg3=(ImageButton)findViewById(R.id.imagenCargada3);
        imagenCarg4=(ImageButton)findViewById(R.id.imagenCargada4);
        //btnAbrirCalendario=(Button)findViewById(R.id.btnAbrirCalendario);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imagenCarg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirGaleria(v);
            }
        });

        imagenCarg2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirGaleria(v);
            }
        });

        imagenCarg3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirGaleria(v);
            }
        });

        imagenCarg4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirGaleria(v);
            }
        });

        btnAgregarAlojamientoMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    Intent intent = builder.build(agregarAlojamientoActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
//                Intent intent = new Intent(agregarAlojamientoActivity.this, AgregarAlojamientoMapaActivity.class);
//                startActivity(intent);
            }
        });


        agregar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String idAlojamiento = registrarAlojamientoDatabase();
                if (!idAlojamiento.equals("")) {
                    subirImagenStorage(idAlojamiento);
                    Intent intent = new Intent(agregarAlojamientoActivity.this, buscarPropiedades.class);
                    startActivity(intent);
                }
            }
        });
        etFecha = (EditText) findViewById(R.id.et_mostrar_fecha_picker);
        //Widget ImageButton del cual usaremos el evento clic para obtener la fecha
        ibObtenerFecha = (ImageButton) findViewById(R.id.ib_obtener_fecha);
        //Evento setOnClickListener - clic
        ibObtenerFecha.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_obtener_fecha:
                obtenerFecha();
                break;
        }
    }

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
                etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
            /**
             *También puede cargar los valores que usted desee
             */
        }, anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();

    }

    public String registrarAlojamientoDatabase() {
        String precio = valor.getText().toString().trim();
        String dscp = descripcion.getText().toString();
        String tipo = spinTipo.getSelectedItem().toString();
        String ciudad = spinCiudad.getSelectedItem().toString();
        String cant = spinHuesp.getSelectedItem().toString();
        String fecha = etFecha.getText().toString();
        Toast.makeText(this, "fecha:" + fecha, Toast.LENGTH_LONG).show();
        if (TextUtils.isEmpty(precio)) {
            Toast.makeText(this, "Se debe ingresar un valor válido", Toast.LENGTH_LONG).show();
            return "";
        }
        if (TextUtils.isEmpty(dscp)) {
            Toast.makeText(this, "Se debe ingresar una descripcion", Toast.LENGTH_LONG).show();
            return "";
        }
        if (TextUtils.isEmpty(fecha)) {
            Toast.makeText(this, "Se debe ingresar una fecha inicial", Toast.LENGTH_LONG).show();
            return "";
        }
        if (ubicacionAlojamiento == null) {
            Toast.makeText(this, "Se debe ingresar un alojamiento", Toast.LENGTH_LONG).show();
            return "";
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Alojamiento alojamiento = new Alojamiento(precio, dscp, tipo, ciudad, cant, user.getUid(), fecha, ubicacionAlojamiento.latitude, ubicacionAlojamiento.longitude);
        myRef = database.getReference("alojamientos");
        String idAloj = myRef.push().getKey();
        myRef.child(idAloj).setValue(alojamiento);
        return idAloj;
    }

    //abrir la galeria de fotos del dispositivo
    public void abrirGaleria(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),
                SELECT_FILE);
    }

    //Re-escritura del metodo onActivity
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri = null;


        String filePath = null;
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    int op = 1;
                    if(selectedImage == null) {
                        selectedImage = data.getData();
                        op = 1;
                    } else if(selectedImage2 == null) {
                        selectedImage2 = data.getData();
                        op = 2;
                    } else if(selectedImage3 == null) {
                        selectedImage3 = data.getData();
                        op = 3;
                    } else if(selectedImage4 == null) {
                        selectedImage4 = data.getData();
                        op = 4;
                    }

                    String selectedPath = selectedImage.getPath();
                    if (requestCode == SELECT_FILE) {

                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                if(op == 1) {
                                    imageStream = getContentResolver().openInputStream(
                                            selectedImage);
                                } else if(op == 2) {
                                    imageStream = getContentResolver().openInputStream(
                                            selectedImage2);
                                } else if(op == 3) {
                                    imageStream = getContentResolver().openInputStream(
                                            selectedImage3);
                                } else if(op == 4) {
                                    imageStream = getContentResolver().openInputStream(
                                            selectedImage4);
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                            // Ponemos nuestro bitmap en un ImageView que tengamos en la vista

                            if(op == 1) {
                                imagenCarg.setImageBitmap(bmp);
                            } else if(op == 2) {
                                imagenCarg2.setImageBitmap(bmp);
                            } else if(op == 3) {
                                imagenCarg3.setImageBitmap(bmp);
                            } else if(op == 4) {
                                imagenCarg4.setImageBitmap(bmp);
                            }

                        }
                    }
                }
                break;
            case PLACE_PICKER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    String address = String.format("Place: %s", place.getAddress());
                    Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
                    ubicacionAlojamiento = place.getLatLng();
                }
                break;
        }
    }

    //Metodo auxiliar para llamar uploadImage
    public void subirImagenStorage(String idAlojamiento) {
        uploadImage(idAlojamiento);
    }

    //agrega la imagen al storage con id del alojamiento como nombre
    public void uploadImage(String idAlojamiento) {

        if (selectedImage != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + idAlojamiento + "/img_1.jpg");
            ref.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }

        if (selectedImage2 != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + idAlojamiento + "/img_2.jpg");
            ref.putFile(selectedImage2)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }

        if (selectedImage3 != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + idAlojamiento + "/img_3.jpg");
            ref.putFile(selectedImage3)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }

        if (selectedImage4 != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + idAlojamiento + "/img_4.jpg");
            ref.putFile(selectedImage4)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(agregarAlojamientoActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

}
