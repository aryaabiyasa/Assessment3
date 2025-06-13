package com.aryama0073.assessment3.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aryama0073.assessment3.BuildConfig
import com.aryama0073.assessment3.R
import com.aryama0073.assessment3.model.Mahasiswa
import com.aryama0073.assessment3.model.User
import com.aryama0073.assessment3.network.ApiStatus
import com.aryama0073.assessment3.network.MahasiswaApi
import com.aryama0073.assessment3.network.UserDataStore
import com.aryama0073.assessment3.ui.theme.Assessment3Theme
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showMahasiswaDialog by remember { mutableStateOf(false) }
    var showHapusDialog by remember { mutableStateOf(false) }
    var mahasiswaToDelete by remember { mutableStateOf<Mahasiswa?>(null) }
    var showUbahDialog by remember { mutableStateOf(false) }
    var mahasiswaToUbah by remember { mutableStateOf<Mahasiswa?>(null) }
    var bitmap: Bitmap? by remember { mutableStateOf(null) }

    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCropperImage(context.contentResolver, it)
        if (bitmap != null) showMahasiswaDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        } else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val options = CropImageContractOptions(null, CropImageOptions(imageSourceIncludeGallery = true, imageSourceIncludeCamera = true, fixAspectRatio = true))
                launcher.launch(options)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.tambah_mahasiswa)
                )
            }
        }
    ) { innerPadding ->
        ScreenContent(
            currentUserId = user.email,
            viewModel = viewModel,
            onDeleteClick = { mahasiswa ->
                mahasiswaToDelete = mahasiswa
                showHapusDialog = true
            },
            onEditClick = { mahasiswa ->
                mahasiswaToUbah = mahasiswa
                showUbahDialog = true
            },
            modifier = Modifier.padding(innerPadding)
        )

        if (showDialog) {
            ProfilDialog(user = user, onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }

        if (showMahasiswaDialog) {
            MahasiswaDialog(
                bitmap = bitmap,
                onDismissRequest = { showMahasiswaDialog = false }
            ) { nama, kelas, suku ->
                viewModel.saveData(user.email, nama, kelas, suku, bitmap!!)
                showMahasiswaDialog = false
            }
        }

        if (showHapusDialog) {
            HapusDialog(
                onDismissRequest = { showHapusDialog = false },
                onConfirmation = {
                    mahasiswaToDelete?.let { viewModel.deleteData(user.email, it.id) }
                    showHapusDialog = false
                }
            )
        }

        if (showUbahDialog && mahasiswaToUbah != null) {
            UbahDialog(
                mahasiswa = mahasiswaToUbah!!,
                onDismissRequest = { showUbahDialog = false }
            ) { updatedMahasiswa ->
                viewModel.updateData(user.email, updatedMahasiswa)
                showUbahDialog = false
            }
        }

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(
    viewModel: MainViewModel,
    currentUserId: String,
    onDeleteClick: (Mahasiswa) -> Unit,
    onEditClick: (Mahasiswa) -> Unit,
    modifier: Modifier = Modifier
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.retrieveData()
    }

    when (status) {
        ApiStatus.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize().padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) { mahasiswa ->
                    ListItem(
                        mahasiswa = mahasiswa,
                        isOwner = mahasiswa.emailUploader == currentUserId,
                        onDeleteClick = { onDeleteClick(mahasiswa) },
                        onEditClick = { onEditClick(mahasiswa) }
                    )
                }
            }
        }
        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData() },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}

@Composable
fun ListItem(
    mahasiswa: Mahasiswa,
    isOwner: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
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
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        if (isOwner) {
            Row(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                Box(
                    modifier = Modifier.size(35.dp)
                        .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(35.dp)
                        .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.hapus),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            Text(
                text = mahasiswa.nama,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Kelas: ${mahasiswa.kelas} | Suku: ${mahasiswa.suku}",
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
    else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCropperImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("Image", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    Assessment3Theme {
        MainScreen()
    }
}