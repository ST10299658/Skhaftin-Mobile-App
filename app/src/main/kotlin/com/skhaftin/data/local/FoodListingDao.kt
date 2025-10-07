package com.skhaftin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.skhaftin.model.FoodListing
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodListing(foodListing: FoodListing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodListings: List<FoodListing>)

    @Update
    suspend fun updateFoodListing(foodListing: FoodListing)

    @Delete
    suspend fun deleteFoodListing(foodListing: FoodListing)

    @Query("SELECT * FROM FoodListing WHERE id = :id")
    suspend fun getFoodListingById(id: String): FoodListing?
}
