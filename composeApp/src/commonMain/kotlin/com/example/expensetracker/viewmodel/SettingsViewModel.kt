// commonMain/viewmodel/SettingsViewModel.kt
package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensetracker.Service.getMicrophoneService
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {

    private val _selectedCurrency = MutableStateFlow(Currency.USD)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency

    private val _isVoiceInputEnabled = MutableStateFlow(false)
    val isVoiceInputEnabled: StateFlow<Boolean> = _isVoiceInputEnabled

    private val _selectedThemeOption = MutableStateFlow(ThemeOption.SYSTEM)
    val selectedThemeOption: StateFlow<ThemeOption> = _selectedThemeOption

    private val _hasMicrophonePermission = MutableStateFlow(false)
    val hasMicrophonePermission: StateFlow<Boolean> = _hasMicrophonePermission

    private val microphoneService = getMicrophoneService()
    init {
        // Check initial permission state
        checkMicrophonePermission()
    }

    fun toggleVoiceInput(enabled: Boolean) {
        _isVoiceInputEnabled.value = enabled

        if (enabled) {
            checkMicrophonePermission()
        }
    }


    fun checkMicrophonePermission() {
        _hasMicrophonePermission.value = microphoneService.hasMicrophonePermission()

        // If we don't have permission but switch is on, turn it off
        if (!_hasMicrophonePermission.value && _isVoiceInputEnabled.value) {
            _isVoiceInputEnabled.value = false
        }
    }


    suspend fun requestMicrophonePermission(): Boolean {
        val granted = microphoneService.requestMicrophonePermission()
        _hasMicrophonePermission.value = granted

        // If permission denied, turn off the switch
        if (!granted) {
            _isVoiceInputEnabled.value = false
        }

        return granted
    }


    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun setThemeOption(option: ThemeOption) {
        _selectedThemeOption.value = option
    }
}