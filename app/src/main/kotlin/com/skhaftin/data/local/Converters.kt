package com.skhaftin.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.skhaftin.model.PickupLocation

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPickupLocation(pickupLocation: PickupLocation?): String? {
        return pickupLocation?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toPickupLocation(json: String?): PickupLocation? {
        return json?.let { gson.fromJson(it, PickupLocation::class.java) }
    }

    @TypeConverter
    fun fromMap(map: Map<String, Boolean>?): String? {
        return map?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMap(json: String?): Map<String, Boolean>? {
        return json?.let { 
            gson.fromJson(it, Map::class.java) as? Map<String, Boolean>
        }
    }
}
