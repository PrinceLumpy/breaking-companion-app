package com.princelumpy.breakvault.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        Move::class, 
        Tag::class, 
        MoveTagCrossRef::class, 
        SavedCombo::class, 
        BattleCombo::class, 
        BattleTag::class,
        BattleComboTagCrossRef::class // Added this
    ],
    version = 7 // Bumped version
)
@TypeConverters(Converters::class)
abstract class AppDB : RoomDatabase() {
    abstract fun moveTagDao(): MoveTagDao
    abstract fun savedComboDao(): SavedComboDao
    abstract fun battleComboDao(): BattleComboDao
    abstract fun battleTagDao(): BattleTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        private val PREPOPULATE_TAGS = listOf("Toprock", "Footwork", "Freeze", "Power")

        fun getDatabase(context: Context): AppDB {
            val appContext = context.applicationContext
                ?: throw IllegalStateException("Application context cannot be null when getting database.")

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    appContext,
                    AppDB::class.java,
                    "combo_generator_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(AppDbCallback(appContext))
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun prepopulateDefaultTags(database: AppDB) {
            val dao = database.moveTagDao()
            val existingTags = dao.getAllTagsList().map { it.name }.toSet()
            PREPOPULATE_TAGS.forEach { tagName ->
                if (!existingTags.contains(tagName)) {
                    dao.addTag(Tag(id = UUID.randomUUID().toString(), name = tagName))
                }
            }
            Log.i("AppDB", "Pre-populated default tags (if any were missing): $PREPOPULATE_TAGS")
        }
    }

    private class AppDbCallback(private val applicationContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateDefaultTags(it)
                }
            } ?: Log.e("AppDbCallback", "INSTANCE was null during onCreate, cannot prepopulate tags.")
        }
    }
}
