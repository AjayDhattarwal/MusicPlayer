package com.ar.musicplayer.components.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import kotlin.math.max

enum class PLAYLIST_ACTIONS {
    CREATE,
    IMPORT,
    MERGE
}

@Composable
fun PlaylistDialog(
    title: String,
    placeholder: String,
    action: PLAYLIST_ACTIONS,
    onDismissRequest: () -> Unit,
    showDialog: Boolean,
    returnedText: (String, PLAYLIST_ACTIONS) -> Unit,
){

    var textFieldValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    if(action == PLAYLIST_ACTIONS.IMPORT
        && textFieldValue.isNotEmpty()
            && getPlaylistType(textFieldValue) == "Unknown"
    ) {
        isError = true
    } else{
        isError = false
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    if(isError){
                        Text(text = "Invalid Url", color = Color.Red)
                    }
                    TextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        placeholder = {
                            Text(text = placeholder, color = Color.Gray)
                        },
                        maxLines = if(PLAYLIST_ACTIONS.CREATE == action) 1 else 6,
                        keyboardOptions = KeyboardOptions(
                            imeAction = if (!isError && textFieldValue.isNotEmpty()){
                                ImeAction.Go
                            }else{
                                ImeAction.Default
                            }
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                if (!isError && textFieldValue.isNotEmpty()) {
                                    returnedText(textFieldValue, action)
                                    onDismissRequest()
                                }
                            },
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if(!isError && textFieldValue.isNotEmpty()){
                            returnedText(textFieldValue, action)
                            onDismissRequest()
                        }
                    }
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

fun getPlaylistType(url: String): String {
    val countryCodePattern = """
        /(us|ca|gb|au|de|fr|it|es|nl|se|no|dk|fi|jp|br|mx|in|sg|hk|my|tw|pl|ru|za|ae|kr)/playlist/
    """.trimIndent().toRegex()

    return when {
        url.startsWith("https://open.spotify.com/playlist/", ignoreCase = true)
                && url.substringAfterLast("/").isNotEmpty() -> "Spotify"
        url.startsWith("https://music.apple.com/", ignoreCase = true)
                && countryCodePattern.containsMatchIn(url)
                && url.substringAfterLast("/").isNotEmpty() -> "Apple Music"
        else -> "Unknown"
    }
}