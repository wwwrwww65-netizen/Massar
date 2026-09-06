package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.MasarDao
import com.example.data.local.entities.AssetEntity
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.BudgetEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.LiabilityEntity
import com.example.data.local.entities.SavedScenarioEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        TransactionEntity::class,
        LiabilityEntity::class,
        AssetEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        SavedScenarioEntity::class,
        ChatMessageEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MasarDatabase : RoomDatabase() {
    abstract fun masarDao(): MasarDao

    companion object {
        @Volatile
        private var INSTANCE: MasarDatabase? = null

        fun getInstance(context: Context): MasarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasarDatabase::class.java,
                    "masar_decision_engine.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
