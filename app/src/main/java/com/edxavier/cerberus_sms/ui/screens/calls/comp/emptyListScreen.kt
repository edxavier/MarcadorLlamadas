package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.R

@Composable
fun NoDataScreen(
    message:String,
    imageId: Int,
    onAction: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    tonalElevation = 4.dp,
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Icon(
                        contentDescription = null,
                        modifier = Modifier
                            .padding(24.dp)
                            .size(64.dp),
                        imageVector = ImageVector.vectorResource(id = imageId),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Las llamadas aparecerán aquí",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onAction != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(onClick = onAction) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_call_24),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marcar ahora")
                    }
                }
            }
        }
    }
}
