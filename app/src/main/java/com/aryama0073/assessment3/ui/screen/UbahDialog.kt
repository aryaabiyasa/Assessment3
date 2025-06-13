package com.aryama0073.assessment3.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aryama0073.assessment3.R
import com.aryama0073.assessment3.model.Mahasiswa
import com.aryama0073.assessment3.network.MahasiswaApi

@Composable
fun UbahDialog(
    mahasiswa: Mahasiswa,
    onDismissRequest: () -> Unit,
    onConfirmation: (Mahasiswa) -> Unit
) {
    var nama by remember { mutableStateOf(mahasiswa.nama) }
    var kelas by remember { mutableStateOf(mahasiswa.kelas) }
    var suku by remember { mutableStateOf(mahasiswa.suku) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(MahasiswaApi.getMahasiswaImageUrl(mahasiswa.image))
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.gambar, mahasiswa.nama),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.loading_img),
                    error = painterResource(id = R.drawable.broken_img),
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                )
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text(text = stringResource(id = R.string.nama_mahasiswa)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = kelas,
                    onValueChange = { kelas = it },
                    label = { Text(text = stringResource(id = R.string.kelas)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = suku,
                    onValueChange = { suku = it },
                    label = { Text(text = stringResource(id = R.string.suku)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = { onDismissRequest() }, modifier = Modifier.padding(8.dp)) {
                        Text(text = stringResource(R.string.batal))
                    }
                    OutlinedButton(
                        onClick = {
                            val updatedMahasiswa = mahasiswa.copy(
                                nama = nama,
                                kelas = kelas,
                                suku = suku
                            )
                            onConfirmation(updatedMahasiswa)
                        },
                        enabled = nama.isNotEmpty() && kelas.isNotEmpty() && suku.isNotEmpty(),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.simpan))
                    }
                }
            }
        }
    }
}