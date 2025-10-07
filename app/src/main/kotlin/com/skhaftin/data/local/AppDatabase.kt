package com.skhaftin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skhaftin.model.FoodListing
import com.skhaftin.model.Item

@Database(entities = [Item::class, FoodListing::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun foodListingDao(): FoodListingDao
}
