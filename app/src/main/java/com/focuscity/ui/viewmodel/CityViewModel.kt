package com.focuscity.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuscity.data.db.AppDatabase
import com.focuscity.data.model.Building
import com.focuscity.data.model.BuildingType
import com.focuscity.data.model.UserProfile
import com.focuscity.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CityState(
    val buildings: List<Building> = emptyList(),
    val coins: Int = 0,
    val selectedBuilding: Building? = null,
    val placingType: BuildingType? = null,
    val errorMessage: String? = null
)

class CityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val _cityState = MutableStateFlow(CityState())
    val cityState: StateFlow<CityState> = _cityState.asStateFlow()

    companion object {
        const val GRID_SIZE = 16
    }

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db)

        // Observe buildings
        viewModelScope.launch {
            repository.allBuildings.collect { buildings ->
                _cityState.value = _cityState.value.copy(buildings = buildings)
            }
        }

        // Observe coins
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                _cityState.value = _cityState.value.copy(coins = profile.coins)
            }
        }
    }

    /** Check if a building of given type can be placed at (x, y) without overlapping */
    fun canPlace(type: BuildingType, x: Int, y: Int, excludeId: Int = -1): Boolean {
        // Bounds check
        if (x < 0 || y < 0 || x + type.width > GRID_SIZE || y + type.height > GRID_SIZE) {
            return false
        }

        // Overlap check
        val buildings = _cityState.value.buildings
        for (b in buildings) {
            if (b.id == excludeId) continue
            val bt = BuildingType.valueOf(b.type)
            val overlapsX = x < b.gridX + bt.width && x + type.width > b.gridX
            val overlapsY = y < b.gridY + bt.height && y + type.height > b.gridY
            if (overlapsX && overlapsY) return false
        }
        return true
    }

    /** Find which building occupies cell (x, y), if any */
    fun getBuildingAt(x: Int, y: Int): Building? {
        return _cityState.value.buildings.find { b ->
            val bt = BuildingType.valueOf(b.type)
            x >= b.gridX && x < b.gridX + bt.width &&
            y >= b.gridY && y < b.gridY + bt.height
        }
    }

    fun startPlacing(type: BuildingType) {
        if (_cityState.value.coins < type.cost) {
            _cityState.value = _cityState.value.copy(
                errorMessage = "Not enough coins! Need ${type.cost}"
            )
            return
        }
        _cityState.value = _cityState.value.copy(
            placingType = type,
            selectedBuilding = null,
            errorMessage = null
        )
    }

    fun placeBuilding(x: Int, y: Int) {
        val type = _cityState.value.placingType ?: return
        if (!canPlace(type, x, y)) {
            _cityState.value = _cityState.value.copy(
                errorMessage = "Can't place here — out of bounds or overlapping"
            )
            return
        }

        viewModelScope.launch {
            repository.placeBuilding(type, x, y)
            _cityState.value = _cityState.value.copy(
                placingType = null,
                errorMessage = null
            )
        }
    }

    fun selectBuilding(building: Building) {
        _cityState.value = _cityState.value.copy(
            selectedBuilding = building,
            placingType = null
        )
    }

    fun deselectBuilding() {
        _cityState.value = _cityState.value.copy(
            selectedBuilding = null,
            placingType = null,
            errorMessage = null
        )
    }

    fun deleteBuilding() {
        val building = _cityState.value.selectedBuilding ?: return
        val type = BuildingType.valueOf(building.type)
        if (type == BuildingType.HALL) {
            _cityState.value = _cityState.value.copy(
                errorMessage = "Can't delete the Hall!"
            )
            return
        }

        viewModelScope.launch {
            repository.deleteBuilding(building)
            _cityState.value = _cityState.value.copy(
                selectedBuilding = null,
                errorMessage = null
            )
        }
    }

    fun moveBuilding(newX: Int, newY: Int) {
        val building = _cityState.value.selectedBuilding ?: return
        val type = BuildingType.valueOf(building.type)

        if (!canPlace(type, newX, newY, excludeId = building.id)) {
            _cityState.value = _cityState.value.copy(
                errorMessage = "Can't move here — out of bounds or overlapping"
            )
            return
        }

        viewModelScope.launch {
            repository.moveBuilding(building, newX, newY)
            _cityState.value = _cityState.value.copy(
                selectedBuilding = null,
                errorMessage = null
            )
        }
    }

    fun cancelPlacing() {
        _cityState.value = _cityState.value.copy(
            placingType = null,
            errorMessage = null
        )
    }

    fun clearError() {
        _cityState.value = _cityState.value.copy(errorMessage = null)
    }
}
