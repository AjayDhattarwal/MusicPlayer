package com.ar.musicplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

class DecoderViewModel : ViewModel() {
    private val _decodedOutput = MutableStateFlow("")
    val decodedOutput: StateFlow<String> = _decodedOutput

    fun decode(input: String) {
        viewModelScope.launch {
            try {
                _decodedOutput.value = decodeDES(input)
            } catch (e: Exception) {
                _decodedOutput.value = "Error: ${e.message}"
            }
        }
    }

    private fun decodeDES(input: String): String {
        val key = "38346591"
        val algorithm = "DES/ECB/PKCS5Padding"

        val keyFactory = SecretKeyFactory.getInstance("DES")
        val desKeySpec = DESKeySpec(key.toByteArray(StandardCharsets.UTF_8))
        val secretKey = keyFactory.generateSecret(desKeySpec)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val encryptedBytes = Base64.getDecoder().decode(input)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        var decoded = String(decryptedBytes, StandardCharsets.UTF_8)

        // Replace ".mp4" pattern
        val pattern = Pattern.compile("\\.mp4.*")
        val matcher = pattern.matcher(decoded)
        decoded = matcher.replaceAll(".mp4")

        // Replace "http:" with "https:"
        decoded = decoded.replace("http:", "https:")

        return decoded
    }
}