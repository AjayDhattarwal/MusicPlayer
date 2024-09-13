package com.ar.musicplayer.data.repository

import com.ar.musicplayer.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class GenerativeAiRepository {

    val jsonSchema = Schema.obj(
        name = "systemInstruction",
        description = "System Instruction",
        Schema.str("type", "Possible values: 'song' or 'album' or if response has is just question then value should be 'question' " ),
        Schema(
            name = "songs",
            description = "List of songs",
            type = FunctionType.ARRAY,
            items = Schema.obj(
                name = "song",
                description = "A song",
                Schema.str("title", "Title of the song"),
                Schema.str("artist", "Artist of the song"),
                Schema.str("album", "Album of the song"),
                Schema.int("release_year", "Release year of the song"),
                Schema.num("duration", "Duration of the song in seconds As Long"),
                Schema.str("genre", "Genre of the song"),
                Schema.str("description", "Description of the song"),
                Schema.str("other", "Other information about the song")
            )
        ),
        Schema(
            name = "artists",
            description = "List of artists",
            type = FunctionType.ARRAY,
            items = Schema.obj(
                name = "artist",
                description = "An artist",
                Schema.str("name", "Name of the artist"),
                Schema.str("description", "Description of the artist"),
                Schema.str("other", "Other information about the artist")
            )
        ),
        Schema.str("genre", "Genre information"),
        Schema.str("description", "Description of the content"),
        Schema.str("other", "Other information")
    )




    private val systemInstruction = Content(
        parts = listOf(
            TextPart("You will respond as an Indian music provider with knowledge of Hindi, Punjabi, Haryanvi, and English music."),
            TextPart("Read questions carefully and provide answers accordingly."),
            TextPart("Your responses should be in JSON format."),
            TextPart("Here is a sample JSON structure for your responses"),
            TextPart("Demonstrate comprehensive knowledge across diverse musical genres and provide relevant examples."),
            TextPart("Your tone should be casual, upbeat, and enthusiastic, spreading the joy of music."),
            TextPart("If a question is not related to music, respond like this : {  description: outputString }")
        )
    )

    suspend fun getAiResponse(prompt: String): GenerateContentResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val generativeModel = GenerativeModel(
//                    modelName = "gemini-1.5-flash",
                    modelName = "gemini-1.5-pro-exp-0827",
                    systemInstruction = systemInstruction,
                    apiKey = BuildConfig.API_KEY,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                        responseSchema = jsonSchema
                    },
                    safetySettings = listOf(
                        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
                    )
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
