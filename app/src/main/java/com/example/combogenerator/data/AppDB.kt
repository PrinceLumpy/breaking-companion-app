package com.example.combogenerator.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Move::class, Tag::class, MoveTagCrossRef::class],
    version = 1
)
abstract class AppDB : RoomDatabase() {
    abstract fun moveTagDao(): MoveTagDao
}