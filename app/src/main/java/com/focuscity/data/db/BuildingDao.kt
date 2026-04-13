package com.focuscity.data.db

import androidx.room.*
import com.focuscity.data.model.Building
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {

    @Insert
    suspend fun insert(building: Building): Long

    @Delete
    suspend fun delete(building: Building)

    @Update
    suspend fun update(building: Building)

    @Query("SELECT * FROM building")
    fun getAllBuildings(): Flow<List<Building>>

    @Query("SELECT * FROM building")
    suspend fun getAllBuildingsOnce(): List<Building>
}
