// commonMain/viewmodel/SettingsViewModel.kt
package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import com.example.expensetracker.service.getMicrophoneService
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

    fun checkMicrophonePermission() {
        _hasMicrophonePermission.value = getMicrophoneService().hasMicrophonePermission()
        println("🔊 ViewModel - Microphone permission: ${_hasMicrophonePermission.value}")

        if (_hasMicrophonePermission.value) {
            _isVoiceInputEnabled.value = true
            println("🔊 Auto-enabling voice input (permission granted)")
        }
    }


    fun toggleVoiceInput(enabled: Boolean) {
        if (enabled && !_hasMicrophonePermission.value) {
            println("🔊 No permission - requesting microphone access")
            getMicrophoneService().requestMicrophonePermission()
            return
        }
        _isVoiceInputEnabled.value = enabled
    }
    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun setThemeOption(option: ThemeOption) {
        _selectedThemeOption.value = option
    }
}