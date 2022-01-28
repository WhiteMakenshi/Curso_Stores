package com.makenshi.stores

import androidx.room.*

@Dao
interface StoreDAO {
    @Query("SELECT * FROM Store")
    fun getAllStores() : MutableList<StoreEntity>

    @Query("SELECT * FROM Store WHERE id = :storeId")
    fun getStoreById(storeId: Long) : StoreEntity

    @Insert
    fun addStore(storeEntity: StoreEntity) : Long

    @Update
    fun updateStore(storeEntity: StoreEntity)

    @Delete
    fun deleteStore(storeEntity: StoreEntity)
}