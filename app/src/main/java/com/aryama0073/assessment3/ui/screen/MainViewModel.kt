package com.aryama0073.assessment3.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryama0073.assessment3.model.Mahasiswa
import com.aryama0073.assessment3.network.ApiStatus
import com.aryama0073.assessment3.network.MahasiswaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Mahasiswa>())
        private set
    var status = MutableStateFlow(ApiStatus.LOADING)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData() {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = MahasiswaApi.service.getMahasiswa()
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(email: String, nama: String, kelas: String, suku: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                MahasiswaApi.service.postMahasiswa(
                    email = email,
                    nama.toRequestBody("text/plain".toMediaTypeOrNull()),
                    kelas.toRequestBody("text/plain".toMediaTypeOrNull()),
                    suku.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )
                retrieveData()

            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun updateData(email: String, mahasiswa: Mahasiswa) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                MahasiswaApi.service.updateMahasiswa(email, mahasiswa.id, mahasiswa)

                retrieveData()

            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(email: String, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = MahasiswaApi.service.deleteMahasiswa(email, id)

                if (response.isSuccessful) {
                    retrieveData()
                } else {
                    throw Exception("Gagal menghapus data. Kode: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
    }

    fun clearMessage() { errorMessage.value = null }
}