package com.aryama0073.assessment3.model

import com.squareup.moshi.Json

data class Mahasiswa(
    val id: String,
    val nama: String,
    val kelas: String,
    val suku: String,
    @Json(name = "image_url")
    val imageUrl: String,
    @Json(name = "email_uploader")
    val emailUploader: String
)