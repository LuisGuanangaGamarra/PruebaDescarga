package com.example.pruebadescarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.pruebadescarga.interfaces.IServiceUpload;
import com.example.pruebadescarga.model.ResponseServicioUpload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoActivity extends AppCompatActivity {
    final private int REQUEST_CODE_PERMISSIONS_CAMARA = 123;
    private Button btnGaleria;
    private VideoView videoContainer;
    private Context context;
    private Runnable subida;
    private Runnable subidaFirebase;
    private int eventVideo = 1;
    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        this.btnGaleria = (Button) findViewById(R.id.btn_video);
        this.videoContainer = (VideoView) findViewById(R.id.videoView);
        context = getApplicationContext();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 23) {
                    int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS_CAMARA);
                    }else {
                        Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
                        videoPickerIntent.setType("video/*");
                        startActivityForResult(videoPickerIntent, eventVideo);
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == eventVideo)
        {
            try{
               final Uri videoUri = data.getData();


                if (videoUri != null) {

                    this.videoContainer.setVideoURI(videoUri);
                    this.videoContainer.start();
                    subida = new Runnable() {
                        @Override
                        public void run() {
                            String path= generatePath(videoUri,context);

                            File file = new File(path);
                            boolean isLeible= file.canRead();
                            int upperbound = 25;
                            Random rand = new Random();
                            int int_random = rand.nextInt(upperbound);
                            String a =String.valueOf(int_random);
                            String extension = MimeTypeMap.getFileExtensionFromUrl(file.toString());;
                            RequestBody requestFile =
                                    RequestBody.create(MediaType.parse( "video/"+ extension),file);

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl("http://192.168.8.233:81")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            IServiceUpload service = retrofit.create(IServiceUpload.class);

                            MultipartBody.Part part = MultipartBody.Part.createFormData("img","tmp"+a+"."+extension,requestFile);
                            Call<ResponseServicioUpload> call = service.uploadFile(part);
                            call.enqueue(new Callback<ResponseServicioUpload>() {
                                @Override
                                public void onResponse(Call<ResponseServicioUpload> call, retrofit2.Response<ResponseServicioUpload> response) {
                                    ResponseServicioUpload resp = response.body();
                                    if(resp.getCodigo()!=0){
                                        Toast.makeText(context,resp.getMensaje(),Toast.LENGTH_LONG).show();
                                    }

                                    else
                                    {
                                        Toast.makeText(context,"Video subido al service php",Toast.LENGTH_LONG).show();
                                    }

                                }

                                @Override
                                public void onFailure(Call<ResponseServicioUpload> call, Throwable t) {
                                    Toast.makeText(context,"Error al subir el archivo php service",Toast.LENGTH_LONG).show();
                                }
                            });
                            try {
                                file.deleteOnExit();
                            } catch (Exception e) {
                                Toast.makeText(context,"Success al subir el archivo firebase",Toast.LENGTH_LONG).show();
                            }

                        }
                    };

                    subidaFirebase = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String path= generatePath(videoUri,context);
                                FileInputStream fileInputStream;
                                File file = new File(path);
                                fileInputStream= new FileInputStream(file);
                                String extension = MimeTypeMap.getFileExtensionFromUrl(file.toString());;
                                Random rand = new Random();
                                int upperbound = 25;
                                //generate random values from 0-24
                                int int_random = rand.nextInt(upperbound);
                                String a =String.valueOf(int_random);
                                StorageReference mountainsRef = mStorageRef.child(a+"."+extension);
                                UploadTask uploadTask = mountainsRef.putStream(fileInputStream);
                                /*Subida de archivos*/
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {

                                        Toast.makeText(context,"Error al subir el archivo firebase",Toast.LENGTH_LONG).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(context,"Success al subir el archivo firebase",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(context,"Success al subir el archivo firebase",Toast.LENGTH_LONG).show();
                            }

                        }
                    };
                    new Thread(subida).start();
                    new Thread(subidaFirebase).start();
                }
            }
             catch (Exception e) {

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.videoContainer.stopPlayback ();
    }
    public String generatePath(Uri uri,Context context) {
        String filePath = null;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;


        if(filePath != null){
            return filePath;
        }

        Cursor cursor = context.getContentResolver().query(uri, new String[] { MediaStore.MediaColumns.DATA }, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath == null ? uri.getPath() : filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_PERMISSIONS_CAMARA:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
                    videoPickerIntent.setType("video/*");
                    startActivityForResult(videoPickerIntent, eventVideo);
                }else{
                    // Permission Denied
                    Toast.makeText(VideoActivity.this, "No se aceptó permisos", Toast.LENGTH_SHORT).show();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}