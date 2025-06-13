package com.aryama0073.assessment3.network

import com.aryama0073.assessment3.model.Mahasiswa
import com.aryama0073.assessment3.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

private const val BASE_URL = "https://api-mobpro-v2.kakashispiritnews.my.id/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface MahasiswaApiService {
    @GET("mahasiswa")
    suspend fun getMahasiswa(): List<Mahasiswa>

    @Multipart
    @POST("mahasiswa")
    suspend fun postMahasiswa(
        @Header("Authorization") email: String,
        @Part("nama") nama: RequestBody,
        @Part("kelas") kelas: RequestBody,
        @Part("suku") suku: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @PUT("mahasiswa/{id}")
    suspend fun updateMahasiswa(
        @Header("Authorization") email: String,
        @Path("id") id: String,
        @Body mahasiswa: Mahasiswa
    ): OpStatus

    @DELETE("mahasiswa/{id}")
    suspend fun deleteMahasiswa(
        @Header("Authorization") email: String,
        @Path("id") id: String
    ): OpStatus
}

object MahasiswaApi {
    val service: MahasiswaApiService by lazy {
        retrofit.create(MahasiswaApiService::class.java)
    }

    fun getMahasiswaImageUrl(filename: String): String {
        return "$BASE_URL$filename"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }