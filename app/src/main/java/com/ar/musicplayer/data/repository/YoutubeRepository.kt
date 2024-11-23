package com.ar.musicplayer.data.repository

import android.content.Context
import android.util.Log
import com.ar.musicplayer.api.YouTubeApi
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.ArtistMap
import com.ar.musicplayer.data.models.MoreInfoResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.toArtist
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.ktor.utils.io.writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
    private val context: Context,
    private val youTubeApi: YouTubeApi
) {

    private suspend fun getYouTubeData(path: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = youTubeApi.getYouTubeData(path)
                response.string()
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun getPlaylistData(path: String): String? {
        return withContext(Dispatchers.IO) {
            val response = youTubeApi.getPlaylistData(path)
            response.string()
        }
    }

    suspend fun getMusicHome():List<Map<String,Any>>  = withContext(Dispatchers.IO) {
        val response = getYouTubeData("music") ?: return@withContext emptyList()
        try {
            val file = File(context.cacheDir, "youtube.txt")
            val searchResults = "{" + Regex("(\"contents\":\\{.*?\"carouselHeaderRenderer\")", RegexOption.DOT_MATCHES_ALL)
                .find(response)?.groupValues?.get(0)?.replace(",\"header\":{\"carouselHeaderRenderer\"", "}")

            file.writeText(searchResults)


            val data = JsonParser.parseString(searchResults).asJsonObject



            val result = data.getAsJsonObject("contents")
                .getAsJsonObject("twoColumnBrowseResultsRenderer")
                .getAsJsonArray("tabs").get(0).asJsonObject
                .getAsJsonObject("tabRenderer").getAsJsonObject("content")
                .getAsJsonObject("sectionListRenderer").getAsJsonArray("contents")

            Log.d("HomeViewModel", "getYoutubeMapData result: $result")



            val items = result.map { element ->
                element.asJsonObject.getAsJsonObject("itemSectionRenderer")
                    .getAsJsonArray("contents")
                    .get(0).asJsonObject
                    .getAsJsonObject("shelfRenderer")
            }

            val finalResult = items.mapNotNull { element ->
                val title = element.getAsJsonObject("title")
                    .getAsJsonArray("runs").get(0).asJsonObject
                    .get("text").asString.trim()

                val itemsList = element.getAsJsonObject("content")
                    .getAsJsonObject("horizontalListRenderer")
                    .getAsJsonArray("items")

                if (title != "Highlights from Global Citizen Live") {
                    mapOf(
                        "title" to title,
                        "playlists" to when {
                            title == "Charts" -> formatChartItems(itemsList)
                            title.contains("Music Videos") -> formatVideoItems(itemsList)
                            else -> formatItems(itemsList)
                        }
                    )
                } else null
            }

           return@withContext finalResult
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error parsing YouTube data: ${e.message}")
            return@withContext emptyList()
        }
    }


    suspend fun ytPlaylistSongs(playlistId: String): List<SongResponse>? {

        val jsonString = getYtPlaylistData(id = playlistId) ?: return null

        val jsonObject = JsonParser.parseString(jsonString).asJsonObject

        val contents = jsonObject
            .getAsJsonObject("contents")
            .getAsJsonObject("twoColumnBrowseResultsRenderer")
            .getAsJsonArray("tabs")
            .get(0).asJsonObject
            .getAsJsonObject("tabRenderer")
            .getAsJsonObject("content")
            .getAsJsonObject("sectionListRenderer")
            .getAsJsonArray("contents")
            .get(0).asJsonObject
            .getAsJsonObject("itemSectionRenderer")
            .getAsJsonArray("contents")
            .get(0).asJsonObject
            .getAsJsonObject("playlistVideoListRenderer")
            .getAsJsonArray("contents")

        val items = contents.mapNotNull { element ->
            val item = element.asJsonObject
                .getAsJsonObject("playlistVideoRenderer")

            println(item)

            val videoId = item?.get("videoId")?.asString
            if (videoId != null) {
                val ytTitle = item.getAsJsonObject("title")
                    .getAsJsonArray("runs").get(0).asJsonObject
                    .get("text").asString
                    .replace(Regex("""\s*\(.*?\)$"""), "")

                val info =  parseSongInfo(ytTitle)
                val subtitle = item.getAsJsonObject("videoInfo")
                    .getAsJsonArray("runs")
                    .map {
                        it.asJsonObject.get("text").asString
                    }.joinToString("")
                val thumbnail = item.getAsJsonObject("thumbnail")
                    .getAsJsonArray("thumbnails")
                    .last()
                    .asJsonObject
                    .get("url").asString

                SongResponse(
                    id = videoId,
                    title = info.title,
                    subtitle = subtitle,
                    image = thumbnail,
                    type = "Youtube",
                    moreInfo = MoreInfoResponse(
                        artistMap = ArtistMap(
                            artists = info.artists
                        )
                    )
                )
            }else
                null

        }

        return items
    }


    ////////////////////// Private Fun //////////////////

    private suspend fun getYtPlaylistData(id: String):  String? {
        val jsonResponse  = getPlaylistData(id) ?: return null

        return try {
            // Extracting ytInitialData using Regex
            val ytInitialDataRegex = Regex("var ytInitialData = (\\{.*?\\});", RegexOption.DOT_MATCHES_ALL)
            val matchResult = ytInitialDataRegex.find(jsonResponse)

            if (matchResult != null) {
                val ytInitialDataJson = matchResult.groupValues[1]

                ytInitialDataJson
            } else {
                println("ytInitialData not found in the response.")
                null
            }
        } catch (e: Exception) {
            println("Error parsing YouTube data: ${e.message}")
            null
        }
    }


    private fun parseSongInfo(song: String): SongInfo {

        // Split by '|' to separate title, album/movie, and artists
        val parts = song.split("|").map { it.trim() }

        val title = parts.getOrNull(0)
            ?.substringBeforeLast("(")
            ?.trim() ?: "Unknown Title"
        val movieOrAlbum = parts.getOrNull(1)

        val artistPattern = "(?i)\\)\\s*([^|]+)\\|([^|]+)".toRegex()

        val matchResult = artistPattern.find(
            song.replace("- Official Video |", ")")

        )

        val firstTwoArtists = matchResult?.groupValues?.subList(1, 3)?.map { it.trim() } ?: emptyList()

        val formattedArtists = firstTwoArtists.joinToString(", ").toArtist()


        // Return the mapped song info
        return  SongInfo(title, movieOrAlbum, formattedArtists)

    }


    private fun formatVideoItems(itemsList: JsonArray): List<Item> {
        return try {
            itemsList.map { item ->
                val video = item.asJsonObject.getAsJsonObject("gridVideoRenderer")

                val songInfo = parseSongInfo(
                    video.getAsJsonObject("title")
                        .get("simpleText")
                        .asString
                )

                Item(
                    title = songInfo.title ,
                    type = "Video",
                    description = video.getAsJsonObject("shortBylineText")
                        .getAsJsonArray("runs")
                        .get(0).asJsonObject
                        .get("text").asString,
                    count = video.getAsJsonObject("shortViewCountText")
                        .get("simpleText").asString,
                    id = video.get("videoId").asString,
                    image = video.getAsJsonObject("thumbnail")
                        .getAsJsonArray("thumbnails")
                        .last().asJsonObject
                        .get("url").asString,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    private fun formatItems(itemsList: JsonArray): List<Item> {
        return try {
            itemsList.map { item ->
                val playlist = item.asJsonObject.getAsJsonObject("lockupViewModel")
                val metadata = playlist.getAsJsonObject("metadata").getAsJsonObject("lockupMetadataViewModel")
                val metadataParts =  metadata.getAsJsonObject("metadata").getAsJsonObject("contentMetadataViewModel")
                    .getAsJsonArray("metadataRows").get(0).asJsonObject.getAsJsonArray("metadataParts").get(0).asJsonObject
                val thumbnailViewModel = playlist.getAsJsonObject("contentImage").getAsJsonObject("collectionThumbnailViewModel")
                    .getAsJsonObject("primaryThumbnail").getAsJsonObject("thumbnailViewModel")

                val contentImage = thumbnailViewModel.getAsJsonObject("image").getAsJsonArray("sources")
                val overlays = thumbnailViewModel.getAsJsonArray("overlays").get(0).asJsonObject
                    .getAsJsonObject("thumbnailOverlayBadgeViewModel").getAsJsonArray("thumbnailBadges")
                    .get(0).asJsonObject.getAsJsonObject("thumbnailBadgeViewModel")


                Item(
                    title = metadata.getAsJsonObject("title").get("content").asString,
                    type = "playlist",
                    description = metadataParts.getAsJsonObject("text").get("content").asString,
                    count = overlays.get("text").asString,
                    id = playlist.get("contentId").asString,
                    image = contentImage.last().asJsonObject.get("url").asString,
                )
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "formatItems: ${e.message}")
            emptyList()
        }
    }

    private fun formatChartItems(itemsList: JsonArray): List<Item> {
        return try {
            itemsList.map { item ->
                val chart = item.asJsonObject.getAsJsonObject("gridPlaylistRenderer")
                Item(
                    title = chart.getAsJsonObject("title")
                        .getAsJsonArray("runs")
                        .get(0).asJsonObject
                        .get("text").asString,
                    type = "Chart",
                    description = chart.getAsJsonObject("shortBylineText")
                        .getAsJsonArray("runs")
                        .get(0).asJsonObject
                        .get("text").asString,
                    count = chart.getAsJsonObject("videoCountText")
                        .getAsJsonArray("runs")
                        .get(0).asJsonObject
                        .get("text").asString,
                    id = chart.getAsJsonObject("navigationEndpoint")
                        .getAsJsonObject("watchEndpoint")
                        .get("playlistId").asString,
                    image = chart.getAsJsonObject("thumbnail")
                        .getAsJsonArray("thumbnails").get(0).asJsonObject.get("url").asString,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }




}

data class Item(
    val title: String,
    val type: String,
    val description: String,
    val count: String,
    val id: String,
    val image: String,
)

private data class SongInfo(
    val title: String,
    val movieOrAlbum: String?,
    val artists: List<Artist>
)






