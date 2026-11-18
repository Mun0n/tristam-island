package com.tristamisland.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [IslandSaveEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class IslandDatabase : RoomDatabase() {
    abstract fun islandSaveDao(): IslandSaveDao
}
