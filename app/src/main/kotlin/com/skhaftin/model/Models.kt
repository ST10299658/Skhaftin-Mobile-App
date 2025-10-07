package com.skhaftin.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.skhaftin.data.local.Converters

// --- Room Entities ---

@Entity(tableName = "item")
data class Item(
    @PrimaryKey
    var id: String,
    var name: String,
    var quantity: String,
    var description: String,
    var location: String,
    var imageUrl: String?,
    var localImagePath: String?,
    var ownerId: String,
    var uniqueCode: String
) {
    @Ignore
    constructor() : this("", "", "", "", "", null, null, "", "")
}

@Entity(tableName = "foodlisting")
@TypeConverters(Converters::class)
data class FoodListing(
    @PrimaryKey
    var id: String,
    var donorId: String,
    var foodName: String,
    var description: String,
    var category: String,
    var expiryDate: String,
    var urgency: String,
    var quantity: String,
    var imageUrl: String?,
    var pickupLocation: PickupLocation?,
    var pickupTime: String,
    var available: Boolean,
    var timestamp: Long
) {
    @Ignore
    constructor() : this("", "", "", "", "", "", "", "", null, null, "", true, 0L)
}

// --- Supporting Models ---

data class PickupLocation(var lat: Double = 0.0, var lng: Double = 0.0, var address: String = "")
