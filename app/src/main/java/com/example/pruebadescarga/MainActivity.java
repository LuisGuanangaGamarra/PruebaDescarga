package com.example.pruebadescarga;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.pruebadescarga.interfaces.IServiceUpload;
import com.example.pruebadescarga.model.ResponseServicioUpload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ImageView selectedImage;
    private Bitmap currentImage;
    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Click en fab button", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        selectedImage = (ImageView) findViewById(R.id.foto);
        Button openGallery = (Button) findViewById(R.id.takeFoto);

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                try {
                    currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    selectedImage.setImageBitmap(currentImage);
                    final Context context = this.getBaseContext();
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            currentImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            Random rand = new Random(); //instance of random class
                            int upperbound = 25;
                            //generate random values from 0-24
                            int int_random = rand.nextInt(upperbound);
                            String a =String.valueOf(int_random);
                            StorageReference mountainsRef = mStorageRef.child(a+".jpg");
                            UploadTask uploadTask = mountainsRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                    Toast.makeText(context,"Error al subir el archivo",Toast.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(context,"Success al subir el archivo",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    };

                    final Runnable s =  new Runnable(){
                        @Override
                        public void run() {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            currentImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            Random rand = new Random(); //instance of random class
                            int upperbound = 25;
                            //generate random values from 0-24
                            int int_random = rand.nextInt(upperbound);
                            String a =String.valueOf(int_random);
                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl("http://192.168.0.107/")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            IServiceUpload service = retrofit.create(IServiceUpload.class);
                            File fi = null;
                            try {
                                fi = File.createTempFile("tmp"+a,".jpeg",null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(fi);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            try {
                                fos.write(data);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            RequestBody requestFile =
                                    RequestBody.create(MediaType.parse("image/jpeg"),fi);
                            MultipartBody.Part part = MultipartBody.Part.createFormData("img", "tmp"+a+".jpeg",requestFile);
                            Call<ResponseServicioUpload> call = service.uploadFile(part);
                            call.enqueue(new Callback<ResponseServicioUpload>() {
                                @Override
                                public void onResponse(Call<ResponseServicioUpload> call, retrofit2.Response<ResponseServicioUpload> response) {
                                    ResponseServicioUpload resp = response.body();
                                    Toast.makeText(context,resp.getMensaje(),Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<ResponseServicioUpload> call, Throwable t) {
                                    Toast.makeText(context,"Error al subir el archivo php service",Toast.LENGTH_LONG).show();
                                }
                            });
                            try {
                                fos.close();
                                fi.deleteOnExit();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    new Thread(r).start();
                    new Thread(s).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
