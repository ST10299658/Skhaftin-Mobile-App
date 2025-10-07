package com.skhaftin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skhaftin.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM item")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM item")
    fun getAllItemsFlow(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Query("DELETE FROM item WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT * FROM item WHERE id = :id")
    suspend fun getItemById(id: String): Item?

    @Query("SELECT * FROM item WHERE name = :name LIMIT 1")
    suspend fun getItemByName(name: String): Item?

    @Query("DELETE FROM item")
    suspend fun deleteAll()
}
