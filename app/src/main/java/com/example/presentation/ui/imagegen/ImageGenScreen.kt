package com.example.presentation.ui.imagegen

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.core.network.GeminiApiClient
import com.example.core.network.GeminiImageClient
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class GeneratedArt(
    val id: String,
    val prompt: String,
    val style: String,
    val resolution: String,
    val aspectRatio: String,
    val imageFile: File,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenScreen() {
    val context = LocalContext.current
    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk") }
    var selectedResolution by remember { mutableStateOf("HD 1080p") }
    var selectedAspect by remember { mutableStateOf("1:1") }
    var isGenerating by remember { mutableStateOf(false) }

    val styles = listOf("Cyberpunk", "Photorealistic", "Anime", "Cinematic", "3D Render", "Oil Painting", "Sci-Fi", "Minimalist")
    val resolutions = listOf("Standard", "HD 1080p", "Ultra 4K")
    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")

    val artGallery = remember { mutableStateListOf<GeneratedArt>() }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun saveBytesToCache(bytes: ByteArray): File {
        val dir = File(context.cacheDir, "generated_images").apply { mkdirs() }
        val file = File(dir, "art_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    fun saveToGallery(file: File): Boolean {
        return try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sara AI")
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun shareImage(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share artwork").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Art & Image Generator", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brush, contentDescription = null, tint = NeonPurpleBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create AI Art & Visual Assets", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Transform your ideas into digital artwork using Gemini.", color = TextSecondaryDark, fontSize = 12.sp)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Describe the image you want to generate...", color = TextMuted) },
                    trailingIcon = {
                        if (promptInput.isNotEmpty()) {
                            IconButton(onClick = { promptInput = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = SoftPinkAccent)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurpleBright,
                        unfocusedBorderColor = BorderPurpleGlow,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .testTag("image_gen_prompt_input")
                )
            }

            item {
                Column {
                    Text("Artistic Style", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(styles) { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                                label = { Text(style, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurpleBright,
                                    selectedLabelColor = AmoledBlack,
                                    containerColor = SurfaceDark,
                                    labelColor = TextSecondaryDark
                                )
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Resolution", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(resolutions) { res ->
                                FilterChip(
                                    selected = selectedResolution == res,
                                    onClick = { selectedResolution = res },
                                    label = { Text(res, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = AmoledBlack)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aspect Ratio", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(aspectRatios) { ratio ->
                                FilterChip(
                                    selected = selectedAspect == ratio,
                                    onClick = { selectedAspect = ratio },
                                    label = { Text(ratio, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftPinkAccent, selectedLabelColor = AmoledBlack)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (promptInput.isNotBlank() && !isGenerating) {
                            isGenerating = true
                            coroutineScope.launch {
                                val apiKey = GeminiApiClient.getApiKey()
                                val result = withContext(Dispatchers.IO) {
                                    GeminiImageClient.generateImage(promptInput, selectedStyle, selectedAspect, apiKey)
                                }
                                result.onSuccess { bytes ->
                                    val file = withContext(Dispatchers.IO) { saveBytesToCache(bytes) }
                                    artGallery.add(
                                        0,
                                        GeneratedArt(
                                            id = System.currentTimeMillis().toString(),
                                            prompt = promptInput,
                                            style = selectedStyle,
                                            resolution = selectedResolution,
                                            aspectRatio = selectedAspect,
                                            imageFile = file,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    snackbarHostState.showSnackbar("Image generated successfully!")
                                }.onFailure { e ->
                                    snackbarHostState.showSnackbar(e.message ?: "Image generation failed")
                                }
                                isGenerating = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_image_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Rendering AI Artwork...", color = AmoledBlack, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmoledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Image", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Generated Artwork Gallery", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            if (artGallery.isEmpty()) {
                item {
                    Text(
                        "No artwork yet — generate your first image above.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }

            items(artGallery, key = { it.id }) { art ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AmoledBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = art.imageFile,
                                contentDescription = art.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, AmoledBlack.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            Surface(
                                color = NeonPurpleBright,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(art.style, color = AmoledBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(art.prompt, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${art.resolution} • Aspect ${art.aspectRatio}", color = TextSecondaryDark, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(art.prompt))
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Prompt copied to clipboard!") }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val saved = withContext(Dispatchers.IO) { saveToGallery(art.imageFile) }
                                            snackbarHostState.showSnackbar(
                                                if (saved) "Saved to Pictures/Sara AI" else "Couldn't save — check storage permission"
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Save", tint = SoftPinkAccent, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { shareImage(art.imageFile) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
