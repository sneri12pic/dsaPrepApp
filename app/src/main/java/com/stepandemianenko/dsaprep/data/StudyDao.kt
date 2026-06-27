package com.stepandemianenko.dsaprep.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_tasks WHERE date = :date ORDER BY id ASC")
    fun getTasksForDate(date: LocalDate): Flow<List<StudyTaskEntity>>

    @Query("SELECT COUNT(*) FROM study_tasks WHERE date = :date")
    suspend fun getTaskCountForDate(date: LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Query("UPDATE study_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean)

    @Query("SELECT * FROM study_tasks")
    fun getAllTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 1")
    fun getCompletedTasks(): Flow<List<StudyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCheckIn(checkIn: DailyCheckInEntity)

    @Query("SELECT * FROM daily_check_ins WHERE date = :date LIMIT 1")
    fun getCheckInByDate(date: LocalDate): Flow<DailyCheckInEntity?>

    @Query("SELECT * FROM daily_check_ins ORDER BY date DESC LIMIT :limit")
    fun getRecentCheckIns(limit: Int): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_check_ins")
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblemSession(session: ProblemSessionEntity): Long

    @Query(
        """
        UPDATE problem_sessions
        SET reviewPattern = :reviewPattern,
            reviewKeyInsight = :reviewKeyInsight,
            reviewMistake = :reviewMistake,
            reviewRewriteStatus = :reviewRewriteStatus,
            reviewFinalTakeaway = :reviewFinalTakeaway
        WHERE id = :sessionId
        """
    )
    suspend fun updateProblemSessionReview(
        sessionId: Long,
        reviewPattern: String?,
        reviewKeyInsight: String?,
        reviewMistake: String?,
        reviewRewriteStatus: String?,
        reviewFinalTakeaway: String?
    )

    @Query("SELECT * FROM problem_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getProblemSessionById(sessionId: Long): ProblemSessionEntity?

    @Query("SELECT * FROM problem_sessions WHERE dateIso = :dateIso ORDER BY startedAtEpochMillis DESC")
    fun getProblemSessionsForDate(dateIso: String): Flow<List<ProblemSessionEntity>>

    @Query("SELECT * FROM problem_sessions ORDER BY startedAtEpochMillis DESC LIMIT :limit")
    fun getRecentProblemSessions(limit: Int): Flow<List<ProblemSessionEntity>>

    @Query("SELECT * FROM problem_sessions ORDER BY startedAtEpochMillis DESC")
    fun getAllProblemSessions(): Flow<List<ProblemSessionEntity>>
}
