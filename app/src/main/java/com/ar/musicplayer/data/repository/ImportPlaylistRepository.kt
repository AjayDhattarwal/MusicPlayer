package com.ar.musicplayer.data.repository

import com.ar.musicplayer.api.ImportSpotifyPlaylist
import com.ar.musicplayer.data.models.ImportPlaylistResponse
import com.ar.musicplayer.data.models.PagingInfo
import com.ar.musicplayer.data.models.SimpleImportTrack
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject

class ImportPlaylistRepository @Inject constructor(
    private val spotifyImport: ImportSpotifyPlaylist
){

    suspend fun importSpotifyPlaylist(url: String): ImportPlaylistResponse {
        val playlistId = url.substringAfterLast('/').substringBefore("?")
        val bearerToken = fetchAccessToken(url.substringBefore("?"))

        val variables = "{\"uri\":\"spotify:playlist:$playlistId\",\"offset\":0,\"limit\":100}"
        val extensions = "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"82cdf2bca2ef1a39bfb09021c43081ba45a2efee14486810899f226b0bebf917\"}}"

        val response = spotifyImport.fetchPlaylistContents(
            authorization = "Bearer $bearerToken",
            variables = variables,
            extensions = extensions,
            operationName = "fetchPlaylist"
        )
        return parseToSimplePlaylist(response, playlistId = playlistId, url =  url)
    }




    private fun parseToSimplePlaylist(jsonElement: JsonObject, playlistId: String, url: String): ImportPlaylistResponse {
        val itemsArray = jsonElement.getAsJsonObject("data")
            .getAsJsonObject("playlistV2")
            .getAsJsonObject("content")
            .getAsJsonArray("items")

        val items = itemsArray.map { itemElement ->
            val itemData = itemElement.asJsonObject.getAsJsonObject("itemV2").getAsJsonObject("data")
            val name = itemData.get("name").asString

            // Accessing artists correctly
            val artistsArray = itemData.getAsJsonObject("artists").getAsJsonArray("items")
            val artists = artistsArray.map { artistElement ->
                artistElement.asJsonObject.getAsJsonObject("profile").get("name").asString
            }

            val uri = itemData.get("uri").asString
            val uid = itemElement.asJsonObject.get("uid").asString

            SimpleImportTrack(name, artists)
        }

        val playlist = jsonElement.getAsJsonObject("data")
            .getAsJsonObject("playlistV2")

        val content = playlist.getAsJsonObject("content")

        val pagingInfo = content.getAsJsonObject("pagingInfo")

        val image = playlist.getAsJsonObject("images")
            .getAsJsonArray("items")
            .get(0)
            .asJsonObject
            .getAsJsonArray("sources")
            .get(0)
            .asJsonObject
            .get("url").asString



        return ImportPlaylistResponse(
            id = playlistId,
            items = items,
            image = image,
            name = playlist.get("name").asString,
            description = playlist.get("description").asString,
            pageUrl = url,
            pagingInfo = PagingInfo(
                limit = pagingInfo.get("limit")?.asString?.toInt() ?: 1,
                offset = pagingInfo.get("offset")?.asString?.toInt()?: 0,
                totalCount = content.get("totalCount")?.asString?.toInt() ?: 0
            )
        )
    }


    suspend fun importApplePlaylist(url: String): ImportPlaylistResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(url).get()

                val jsonElement = document.select("script[id=serialized-server-data]").firstOrNull()

                return@withContext if (jsonElement != null) {
                    val jsonData = jsonElement.html()
                    parseAppleToSimplePlaylist(jsonData).copy(pageUrl = url)
                } else {
                    println("No JSON data found in the script tag.")
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseAppleToSimplePlaylist(jsonString: String): ImportPlaylistResponse {
        val rootElement = JsonParser.parseString(jsonString)
        val rootObject = rootElement.asJsonArray.get(0).asJsonObject
        val dataObject: JsonObject = rootObject.getAsJsonObject("data")

        val pageMetrics: JsonObject = dataObject.getAsJsonObject("pageMetrics")
            .getAsJsonObject("pageFields")

        val pageUrl: String = pageMetrics.get("pageUrl").asString
        val pageId: String = pageMetrics.get("pageId").asString


        val sections = dataObject.getAsJsonArray("sections")

        val firstSectionItems = sections
            .get(0)
            .asJsonObject
            .getAsJsonArray("items")
            .get(0)
            .asJsonObject

        val artWork = firstSectionItems.getAsJsonObject("artwork")

        val image = artWork
            .getAsJsonObject("dictionary")
            .get("url").asString
            .replace("{w}x{h}", "300x300")
            .replace("{f}", "webp")

        val updateTime = firstSectionItems.get("quaternaryTitle").asString

        val modalPresentationDescriptor = firstSectionItems.getAsJsonObject("modalPresentationDescriptor")

        val headerTitle  = modalPresentationDescriptor.get("headerTitle").asString
        val headerSubtitle = modalPresentationDescriptor.get("headerSubtitle").asString
        val paragraphText = if (modalPresentationDescriptor?.get("paragraphText") !is JsonNull) {
            modalPresentationDescriptor?.get("paragraphText")?.asString ?: ""
        } else {
            ""
        }



        val trackCount = firstSectionItems.get("trackCount").asInt

        val secondSectionItems = sections.get(1)
            .asJsonObject
            .getAsJsonArray("items")

        val items = secondSectionItems.map { song ->
            SimpleImportTrack(
                name = song.asJsonObject.get("title").asString,
                artists = song.asJsonObject.get("artistName").asString.split(",").map { artistName ->
                    artistName.trim()
                },
            )
        }

        val description = sections.get(2)
            .asJsonObject
            .getAsJsonArray("items")
            .get(0)
            .asJsonObject
            .get("description")
            .asString


        return ImportPlaylistResponse(
            id = pageId,
            image = image,
            items = items,
            name = headerTitle + headerSubtitle,
            description = "$description $updateTime",
            pageUrl = pageUrl,
            pagingInfo = PagingInfo(
                totalCount = trackCount
            )
        )


    }


    private suspend fun fetchAccessToken(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Referer", "https://open.spotify.com/")
                    .get()

                // Extract the session script
                val sessionScript = document.select("script#session").first()
                val sessionJson = sessionScript?.data()

                sessionJson?.let { extractAccessToken(it) } ?: run {
                    println("Session script not found.")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Function to extract access token from the JSON string
    private fun extractAccessToken(json: String): String? {
        val regex = """"accessToken":"(.*?)"""".toRegex()
        return regex.find(json)?.groups?.get(1)?.value
    }


}




