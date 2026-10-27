package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FormViewModel(
    private val stateHandle: SavedStateHandle,
    private val userPrefs: UserPreferences
) : ViewModel() {

    // Nivel 2: Estado que sobrevive a Muerte del Proceso (Process Death / LMK)
    var email by mutableStateOf(stateHandle.get<String>("email_key") ?: "")
        private set

    fun updateEmail(newEmail: String) {
        email = newEmail
        stateHandle["email_key"] = newEmail
    }

    // Nivel 3: Flujo de datos desde disco (DataStore) expuesto para la UI
    val nameFromDisk: LiveData<String> = userPrefs.userName.asLiveData()

    // Estado para el Desafío: Indicador de guardado exitoso visible por 1 segundo
    var isSavingIndicatorVisible by mutableStateOf(false)
        private set

    fun saveName(newName: String) {
        viewModelScope.launch {
            userPrefs.saveName(newName)
            isSavingIndicatorVisible = true
            delay(1000)
            isSavingIndicatorVisible = false
        }
    }
}
