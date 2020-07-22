package com.example.pruebadescarga;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.pruebadescarga.interfaces.IServiceUpload;
import com.example.pruebadescarga.model.ResponseServicioUpload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoActivity extends AppCompatActivity {

    private Button btnGaleria;
    private VideoView videoContainer;
    private Context context;
    private Runnable subida;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        this.btnGaleria = (Button) findViewById(R.id.btn_video);
        this.videoContainer = (VideoView) findViewById(R.id.videoView);
        context = getApplicationContext();
        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
                videoPickerIntent.setType("video/*");
                startActivityForResult(videoPickerIntent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1)
        {
            try{
               final Uri videoUri = data.getData();


                if (videoUri != null) {

                    this.videoContainer.setVideoURI(videoUri);
                    this.videoContainer.start();
                    subida = new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(videoUri.getPath());
                            int upperbound = 25;
                            Random rand = new Random();
                            int int_random = rand.nextInt(upperbound);
                            String a =String.valueOf(int_random);
                            String extension =MimeTypeMap.getFileExtensionFromUrl(videoUri.toString());
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
                                    Toast.makeText(context,resp.getMensaje(),Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<ResponseServicioUpload> call, Throwable t) {
                                    Toast.makeText(context,"Error al subir el archivo php service",Toast.LENGTH_LONG).show();
                                }
                            });
                            try {
                                file.deleteOnExit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    };

                    new Thread(subida).start();
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
}