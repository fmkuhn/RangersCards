package com.rangerscards.ui.cards.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.rangerscards.R
import com.rangerscards.data.objects.ImageSrc
import com.rangerscards.ui.theme.CustomTheme

@Composable
fun FullCardImageContainer(imageSrc: String?) {
    Surface(
        color = CustomTheme.colors.l30,
        shape = CustomTheme.shapes.large,
        modifier = Modifier.size(225.dp, 315.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ImageSrc.imageSrc + imageSrc)
                .build(),
            placeholder = painterResource(R.drawable.broken_image_32dp),
            error = painterResource(R.drawable.broken_image_32dp),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}