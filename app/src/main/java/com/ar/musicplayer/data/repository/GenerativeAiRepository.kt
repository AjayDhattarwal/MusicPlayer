package com.ar.musicplayer.data.repository

import com.ar.musicplayer.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class GenerativeAiRepository {

    // Define the system instruction as a constant for better maintainability
    private val systemInstructionJson = """
        {
            "type": "string",  // Possible values: "song" or "album"
            "songs": [
                {
                    "title": "string",
                    "artist": "string",
                    "album": "string",
                    "release_year": "integer",
                    "duration": "long",
                    "genre": "string",
                    "description": "string",
                    "other": "string"
                },
                {
                    "title": "string",
                    "artist": "string",
                    "album": "string",
                    "release_year": "integer",
                    "duration": "long",
                    "genre": "string",
                    "description": "string",
                    "other": "string"
                }
            ],
            "artists": [
                {
                    "name": "string",
                    "description": "string",
                    "other": "string"
                },
                {
                    "name": "string",
                    "description": "string",
                    "other": "string"
                }
            ],
            "genre": "string",
            "description": "string",
            "other": "string"
        }
    """

    private val systemInstruction = Content(
        parts = listOf(
            TextPart("You will respond as an Indian music provider with knowledge of Hindi, Punjabi, Haryanvi, and English music."),
            TextPart("Read questions carefully and provide answers accordingly."),
            TextPart("Your responses should be in JSON format."),
            TextPart("Here is a sample JSON structure for your responses: $systemInstructionJson"),
            TextPart("Demonstrate comprehensive knowledge across diverse musical genres and provide relevant examples."),
            TextPart("Your tone should be casual, upbeat, and enthusiastic, spreading the joy of music."),
            TextPart("If a question is not related to music, respond with: 'That is beyond my knowledge. Please ask another question.'")
        )
    )

    suspend fun getAiResponse(prompt: String): GenerateContentResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    systemInstruction = systemInstruction,
                    apiKey = BuildConfig.API_KEY
                )

                val response = generativeModel.generateContent(prompt)
                println("AI Response: ${response.text}")
                response
            } catch (e: Exception) {
                println( "Failed to get AI response ${e.message}")
                null
            }
        }
    }
}
