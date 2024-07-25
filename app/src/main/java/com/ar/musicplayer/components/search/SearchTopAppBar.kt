package com.ar.musicplayer.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange =  onSearchTextChange,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(percent = 10)
            )
            .onFocusChanged {
                if(!it.isFocused){
                    keyboardController?.hide()
                }
            },
        placeholder = { Text(text = "Music, Artists, and Album", fontSize = 14.sp, color = Color.Gray)},
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "clear",
                modifier = Modifier.clickable { onCloseClicked() }
            )

        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.DarkGray,
            unfocusedTextColor = Color.Gray,
            focusedLeadingIconColor = Color.DarkGray,
            unfocusedLeadingIconColor = Color.Gray,
            focusedTrailingIconColor = Color.DarkGray,
            unfocusedTrailingIconColor = Color.Gray,

            ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        singleLine = true
    )
}
