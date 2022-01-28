package com.makenshi.stores

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(StoreEntity::class), version = 2, exportSchema = false)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao() : StoreDAO
}