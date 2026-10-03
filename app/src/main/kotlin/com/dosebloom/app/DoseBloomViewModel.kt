package com.dosebloom.app

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dosebloom.app.data.DoseBloomDatabase
import com.dosebloom.app.data.DoseBloomRepository
import com.dosebloom.app.data.IntakeStatus
import com.dosebloom.app.data.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class DoseBloomUiState(
    val selectedProfile: String = "Я",
    val darkMode: Boolean = false,
    val language: String = "system",
    val medicines: List<Medicine> = emptyList(),
    val profiles: List<String> = listOf("Я")
)

@OptIn(ExperimentalCoroutinesApi::class)
class DoseBloomViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DoseBloomRepository = DoseBloomRepository(DoseBloomDatabase.get(application)),
    private val settings: SettingsRepository = SettingsRepository(application)
) : AndroidViewModel(application) {

    val selectedProfile: StateFlow<String> = settings.selectedProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        "Я"
    )

    val darkMode: StateFlow<Boolean> = settings.darkMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false
    )

    val language: StateFlow<String> = settings.language.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        "system"
    )

    val medicines: StateFlow<List<Medicine>> = selectedProfile
        .flatMapLatest(repository::observeMedicines)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val profiles: StateFlow<List<String>> = repository.observeProfiles().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        listOf("Я")
    )

    val uiState: StateFlow<DoseBloomUiState> = combine(
        selectedProfile,
        darkMode,
        language,
        medicines,
        profiles
    ) { profile, dark, lang, meds, profs ->
        DoseBloomUiState(
            selectedProfile = profile,
            darkMode = dark,
            language = lang,
            medicines = meds,
            profiles = profs
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DoseBloomUiState())

    fun migrateLegacySettings() = viewModelScope.launch {
        val legacy = getApplication<Application>().getSharedPreferences("MainActivity", Application.MODE_PRIVATE)
        if (settings.selectedProfile.first() == "Я") {
            val legacyProfile = legacy.getString("selected_profile", null)
            if (!legacyProfile.isNullOrBlank()) settings.setSelectedProfile(legacyProfile)
        }
        if (!settings.darkMode.first() && legacy.getBoolean("dark_mode", false)) settings.setDarkMode(true)
    }

    fun observeIntakes(date: String): Flow<List<Intake>> = repository.observeIntakes(date)
    fun observeIntakes(from: String, to: String): Flow<List<Intake>> = repository.observeIntakes(from, to)

    fun takeDose(medicineId: Long, date: String, time: String) = viewModelScope.launch {
        repository.takeDose(medicineId, date, time)
    }

    fun undoDose(medicineId: Long, date: String, time: String) = viewModelScope.launch {
        repository.undoDose(medicineId, date, time)
    }

    fun takeAsNeeded(medicineId: Long) = viewModelScope.launch {
        repository.takeAsNeeded(medicineId)
    }

    fun restock(medicineId: Long, amount: Int) = viewModelScope.launch {
        repository.restock(medicineId, amount)
    }

    fun recordIntake(medicineId: Long, date: String, time: String, status: IntakeStatus) = viewModelScope.launch {
        repository.recordIntake(medicineId, date, time, status)
    }

    fun saveMedicine(medicine: Medicine) = viewModelScope.launch {
        if (medicine.id == 0L) repository.addMedicine(medicine) else repository.updateMedicine(medicine)
    }

    fun deleteMedicine(id: Long) = viewModelScope.launch {
        repository.deleteMedicine(id)
    }

    fun addProfile(name: String) = viewModelScope.launch {
        repository.addProfile(name)
    }

    fun removeProfile(name: String) = viewModelScope.launch {
        repository.removeProfile(name)
    }

    fun selectProfile(name: String) = viewModelScope.launch {
        settings.setSelectedProfile(name)
    }

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        settings.setDarkMode(enabled)
    }

    fun setLanguage(value: String) = viewModelScope.launch {
        settings.setLanguage(value)
    }
}
