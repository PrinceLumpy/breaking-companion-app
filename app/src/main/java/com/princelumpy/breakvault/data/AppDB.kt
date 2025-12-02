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
        BattleComboTagCrossRef::class
    ],
    version = 7
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
                    .addCallback(AppDbCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun prepopulateExampleData(database: AppDB) {
            val moveDao = database.moveTagDao()
            val savedComboDao = database.savedComboDao()
            val battleDao = database.battleComboDao()
            val battleTagDao = database.battleTagDao()

            // --- 1. Tags ---
            val tagsToEnsure = listOf("Toprock", "Footwork", "Freeze", "Power")
            val tagMap = mutableMapOf<String, String>() // Name -> ID

            // Load existing tags first to avoid duplicates if not empty
            val existingTags = moveDao.getAllTagsList()
            existingTags.forEach { tagMap[it.name] = it.id }

            tagsToEnsure.forEach { name ->
                if (!tagMap.containsKey(name)) {
                    val newTag = Tag(id = UUID.randomUUID().toString(), name = name)
                    moveDao.addTag(newTag)
                    tagMap[name] = newTag.id
                }
            }

            // --- 2. Moves (Only if empty) ---
            if (moveDao.getAllMovesList().isEmpty()) {
                val movesData = listOf(
                    Triple("6-Step", "Footwork", "m1"),
                    Triple("CC", "Footwork", "m2"),
                    Triple("Windmill", "Power", "m3"),
                    Triple("Baby Freeze", "Freeze", "m4"),
                    Triple("Toprock Basic", "Toprock", "m5"),
                    Triple("Backspin", "Power", "m6")
                )

                movesData.forEach { (moveName, tagName, _) ->
                    val realMoveId = UUID.randomUUID().toString()
                    moveDao.addMove(Move(id = realMoveId, name = moveName))
                    tagMap[tagName]?.let { tagId ->
                        moveDao.link(MoveTagCrossRef(moveId = realMoveId, tagId = tagId))
                    }
                }
                Log.i("AppDB", "Populated 6 example moves.")
            }

            // --- 3. Battle Tags ---
            val battleTagsToEnsure = listOf("Aggressive", "Musicality")
            val battleTagMap = mutableMapOf<String, String>()

            val existingBattleTags = battleTagDao.getAllBattleTagsList()
            existingBattleTags.forEach { battleTagMap[it.name] = it.id }

            battleTagsToEnsure.forEach { name ->
                if (!battleTagMap.containsKey(name)) {
                    val newTag = BattleTag(id = UUID.randomUUID().toString(), name = name)
                    battleTagDao.insertBattleTag(newTag)
                    battleTagMap[name] = newTag.id
                }
            }

            // --- 4. Battle Combos (Only if empty) ---
            if (battleDao.getAllBattleCombosList().isEmpty()) {
                val battleCombosData = listOf(
                    Triple(
                        BattleCombo(id = UUID.randomUUID().toString(), description = "Windmill -> Backspin -> Freeze", energy = EnergyLevel.HIGH, status = TrainingStatus.READY),
                        "Aggressive", "Power Set 1"
                    ),
                    Triple(
                        BattleCombo(id = UUID.randomUUID().toString(), description = "Smooth transitions to CC", energy = EnergyLevel.MEDIUM, status = TrainingStatus.READY),
                        "Musicality", "Footwork Flow"
                    ),
                    Triple(
                        BattleCombo(id = UUID.randomUUID().toString(), description = "Aggressive Toprock to Drop", energy = EnergyLevel.HIGH, status = TrainingStatus.TRAINING),
                        "Aggressive", "Burn Round"
                    ),
                    Triple(
                        BattleCombo(id = UUID.randomUUID().toString(), description = "Slow intro to floor", energy = EnergyLevel.LOW, status = TrainingStatus.READY),
                        "Musicality", "Intro Set"
                    )
                )

                battleCombosData.forEach { (combo, tagName, _) ->
                    battleDao.insertBattleCombo(combo)
                    // Link Tags
                    battleTagMap[tagName]?.let { tagId ->
                        battleDao.insertBattleComboTagCrossRef(BattleComboTagCrossRef(battleComboId = combo.id, battleTagId = tagId))
                    }
                }
                Log.i("AppDB", "Populated 4 battle combos.")
            }

            // --- 5. Saved Combos (Only if empty) ---
            if (savedComboDao.getAllSavedCombosList().isEmpty()) {
                val savedCombosData = listOf(
                    Pair("Classic Footwork", listOf("6-Step", "CC")),
                    Pair("Power Finisher", listOf("Windmill", "Baby Freeze")),
                    Pair("Top to Down", listOf("Toprock Basic", "Backspin"))
                )

                savedCombosData.forEach { (name, moves) ->
                    savedComboDao.insertSavedCombo(SavedCombo(name = name, moves = moves))
                }
                Log.i("AppDB", "Populated 3 saved combos.")
            }
        }
    }

    private class AppDbCallback() : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateExampleData(it)
                }
            } ?: Log.e("AppDbCallback", "INSTANCE was null during onCreate, cannot prepopulate tags.")
        }
    }
}
