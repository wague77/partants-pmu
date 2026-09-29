package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccessCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessCodeDao {
    @Query("SELECT * FROM access_codes ORDER BY createdAt DESC")
    fun getAllCodes(): Flow<List<AccessCodeEntity>>

    @Query("SELECT * FROM access_codes WHERE code = :code LIMIT 1")
    suspend fun getCode(code: String): AccessCodeEntity?

    @Query("SELECT * FROM access_codes WHERE isActivatedOnDevice = 1 LIMIT 1")
    fun getActiveDeviceCodeFlow(): Flow<AccessCodeEntity?>

    @Query("SELECT * FROM access_codes WHERE isActivatedOnDevice = 1 LIMIT 1")
    suspend fun getActiveDeviceCode(): AccessCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: AccessCodeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(codes: List<AccessCodeEntity>)

    @Update
    suspend fun updateCode(code: AccessCodeEntity)

    @Delete
    suspend fun deleteCode(code: AccessCodeEntity)

    @Query("DELETE FROM access_codes WHERE code = :code")
    suspend fun deleteCodeByValue(code: String)

    @Query("UPDATE access_codes SET isActivatedOnDevice = 0")
    suspend fun clearActiveDeviceCodes()
}
