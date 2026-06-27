package com.stepandemianenko.dsaprep.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [StudyTaskEntity::class, DailyCheckInEntity::class, ProblemSessionEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var instance: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_plan_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `problem_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `problemTitle` TEXT NOT NULL,
                        `problemLink` TEXT,
                        `difficulty` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `startedAtEpochMillis` INTEGER NOT NULL,
                        `finishedAtEpochMillis` INTEGER,
                        `durationSeconds` INTEGER NOT NULL,
                        `goalMinutes` INTEGER NOT NULL,
                        `solvedStatus` TEXT NOT NULL,
                        `timeComplexity` TEXT,
                        `spaceComplexity` TEXT,
                        `mainApproach` TEXT,
                        `mistakeOrBlocker` TEXT,
                        `confidence` INTEGER NOT NULL,
                        `reviewPattern` TEXT,
                        `reviewKeyInsight` TEXT,
                        `reviewMistake` TEXT,
                        `reviewRewriteStatus` TEXT,
                        `reviewFinalTakeaway` TEXT,
                        `dateIso` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_problem_sessions_dateIso` ON `problem_sessions` (`dateIso`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_problem_sessions_topic` ON `problem_sessions` (`topic`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_problem_sessions_difficulty` ON `problem_sessions` (`difficulty`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_problem_sessions_startedAtEpochMillis` ON `problem_sessions` (`startedAtEpochMillis`)")
            }
        }
    }
}
