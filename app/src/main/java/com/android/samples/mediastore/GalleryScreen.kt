package com.android.samples.mediastore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: MainActivityViewModel = viewModel()) {
    val images by viewModel.images.observeAsState(emptyList())
    val notification by viewModel.notification.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadImages()
    }

    var isInitial by remember { mutableStateOf(true) }

    LaunchedEffect(notification) {
        if(isInitial) {
            isInitial = false
        } else {
            notification?.let { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

    }
//    val scope = rememberCoroutineScope()
//    val lifecycleOwner = LocalLifecycleOwner.current
//    viewModel.notification.observe(lifecycleOwner, Observer { message ->
//        scope.launch {
//            snackbarHostState.showSnackbar(message)
//        }
//    })
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
            )
        }
    ) { innerPadding ->
        if (images.isEmpty()) {
            Text(
                text = "No images found!",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
//                contentPadding = PaddingValues(
//                    top = innerPadding.calculateTopPadding() + 8.dp,
//                    bottom = innerPadding.calculateBottomPadding() + 8.dp,
//                    start = 8.dp,
//                    end = 8.dp
//                ),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(images) { imageFile ->
                    ImageItem(imageFile,{
                        viewModel.deleteImage(imageFile)
                    })
                }
            }
        }
    }
}
@Composable
fun ImageItem(imageFile: MediaStoreImage,
              onDelete: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable(
                onClick = {
                    onDelete()
                }
            )
    ) {
        AsyncImage(
            model = imageFile.contentUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

