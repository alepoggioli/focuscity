package com.focuscity.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuscity.data.db.AppDatabase
import com.focuscity.data.model.UserProfile
import com.focuscity.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    val userProfile: StateFlow<UserProfile>
    val todayMinutes: StateFlow<Int>

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db)

        userProfile = repository.userProfile
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

        todayMinutes = flow {
            while (true) {
                emit(repository.getTotalMinutesToday())
                kotlinx.coroutines.delay(30_000) // Refresh every 30s
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        viewModelScope.launch {
            repository.ensureUserProfileExists()
            repository.initializeCity()
        }
    }
}
