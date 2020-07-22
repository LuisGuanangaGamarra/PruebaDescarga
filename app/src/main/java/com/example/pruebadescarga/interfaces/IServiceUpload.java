package com.example.pruebadescarga.interfaces;

import com.example.pruebadescarga.model.ResponseServicioUpload;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IServiceUpload {
    @Multipart
    @POST("/")
    Call<ResponseServicioUpload> uploadFile(@Part MultipartBody.Part img);
}
