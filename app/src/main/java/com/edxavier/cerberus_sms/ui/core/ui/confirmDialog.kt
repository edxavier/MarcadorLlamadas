package com.edxavier.cerberus_sms.ui.core.ui

import android.icu.text.CaseMap.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.launch

@Composable
fun ConfirmDialog(
    title: String,
    content: AnnotatedString,
    onConfirm:() -> Unit,
    onDismiss:() -> Unit,
    onCancel:() -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            )
            { Text(text = "OK") }
        },
        dismissButton = {
            TextButton(onClick = {
                onCancel()
            })
            { Text(text = "Cancelar") }
        },
        title = { Text(text = title, color = MaterialTheme.colorScheme.tertiary) },
        text = {
            Text(text = content )
        }
    )
}