package com.aryama0073.assessment3.model

import com.squareup.moshi.Json

data class Mahasiswa(
    val id: String,
    val nama: String,
    val kelas: String,
    val suku: String,

    @Json(name = "image")
    private val _image: String? = null,

    @Json(name = "image_url")
    private val _imageUrl: String? = null,

    @Json(name = "email_uploader")
    val emailUploader: String
) {
    val image: String
        get() = _image ?: _imageUrl ?: ""
}