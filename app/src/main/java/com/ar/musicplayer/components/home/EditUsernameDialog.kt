package com.ar.musicplayer.components.home



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun EditUsernameDialog(
    initialUsername: String,
    onDismissRequest: () -> Unit,
    onUsernameChange: (String) -> Unit
) {
    var username by remember { mutableStateOf(TextFieldValue(initialUsername)) }

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = "Edit Username") },
        text = {
            Column {
                Text("Username")
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                        .padding(8.dp),
                    maxLines = 1,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onUsernameChange(username.text)
                onDismissRequest()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text("Cancel")
            }
        }
    )
}
