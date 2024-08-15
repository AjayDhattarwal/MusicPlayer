package com.ar.musicplayer.components.library

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun PlaylistDialog(
    title: String,
    onDismissRequest: () -> Unit,
    showDialog: Boolean,
    returnedText: (String) -> Unit
){

    var textFieldValue by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    TextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { returnedText(textFieldValue) }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        )
    }

}