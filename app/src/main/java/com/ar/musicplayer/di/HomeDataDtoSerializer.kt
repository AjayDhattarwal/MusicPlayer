package com.ar.musicplayer.di

import android.util.Log
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.HomeListItem
import com.ar.musicplayer.data.models.MoreInfoHomeList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject


object HomeDataDtoSerializer : KSerializer<HomeListItem> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = HomeListItem::class.java.name
    ) {
        element<String?>("id")
        element<String?>("title")
        element<String?>("subtitle")
        element<String?>("header_desc")
        element<String?>("type")
        element<String?>("perma_url")
        element<String?>("image")
        element<String?>("language")
        element<String?>("year")
        element<String?>("play_count")
        element<String?>("explicit_content")
        element<String?>("list_count")
        element<String?>("list_type")
        element<String?>("list")
        element<MoreInfoHomeList?>("more_info")
        element<Int?>("count")
    }


    override fun deserialize(decoder: Decoder): HomeListItem = decoder.decodeStructure(descriptor) {
        var id: String? = null
        var title: String? = null
        var subtitle: String? = null
        var headerDesc: String? = null
        var type: String? = null
        var permaUrl: String? = null
        var image: String? = null
        var language: String? = null
        var year: String? = null
        var playCount: String? = null
        var explicitContent: String? = null
        var listCount: String? = null
        var listType: String? = null
        var list: String? = null
        var moreInfo: MoreInfoHomeList? = null
        var count: Int? = 0

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> id = decodeStringElement(descriptor, index)
                1 -> title = decodeStringElement(descriptor, index)
                2 -> subtitle = decodeStringElement(descriptor, index)
                3 -> headerDesc = decodeStringElement(descriptor, index)
                4 -> type = decodeStringElement(descriptor, index)
                5 -> permaUrl = decodeStringElement(descriptor, index)
                6 -> image = decodeStringElement(descriptor, index)
                7 -> language = decodeStringElement(descriptor, index)
                8 -> year = decodeStringElement(descriptor, index)
                9 -> playCount = decodeStringElement(descriptor, index)
                10 -> explicitContent = decodeStringElement(descriptor, index)
                11 -> listCount = decodeStringElement(descriptor, index)
                12 -> listType = decodeStringElement(descriptor, index)
                13 -> list = decodeStringElement(descriptor, index)
                14 -> {
                    val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("Expected Json Decoder")

                    val element = jsonDecoder.decodeJsonElement()

                    moreInfo = if (element is JsonObject) {
                        jsonDecoder.json.decodeFromJsonElement(MoreInfoHomeList.serializer(), element)
                    } else {
                        null
                    }
                }
                15 -> count = decodeIntElement(descriptor, index)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure HomeListItem(
            id = id, title = title, subtitle = subtitle, headerDesc = headerDesc, type = type, permaUrl = permaUrl, image = image, language = language,
            year = year, playCount = playCount, explicitContent =  explicitContent, listCount = listCount, listType = listType, list = list,
            moreInfoHomeList = moreInfo, count = count
        )
    }


    override fun serialize(encoder: Encoder, value: HomeListItem) = encoder.encodeStructure(
        descriptor
    ) {
        encodeStringElement(descriptor, 0, value.id ?: "")
        encodeStringElement(descriptor, 1, value.title ?: "")
        encodeStringElement(descriptor, 2, value.subtitle ?: "")
        encodeStringElement(descriptor, 3, value.headerDesc ?: "")
        encodeStringElement(descriptor, 4, value.type ?: "")
        encodeStringElement(descriptor, 5, value.permaUrl ?: "")
        encodeStringElement(descriptor, 6, value.image ?: "")
        encodeStringElement(descriptor, 7, value.language ?: "")
        encodeStringElement(descriptor, 8, value.year ?: "")
        encodeStringElement(descriptor, 9, value.playCount ?: "")
        encodeStringElement(descriptor, 10, value.explicitContent ?: "")
        encodeStringElement(descriptor, 11, value.listCount ?: "")
        encodeStringElement(descriptor, 12, value.listType ?: "")
        encodeStringElement(descriptor, 13, value.list ?: "")
        encodeNullableSerializableElement(descriptor, 14, MoreInfoHomeList.serializer(), value.moreInfoHomeList)
        encodeIntElement(descriptor, 15, value.count ?: 0)


    }


}


