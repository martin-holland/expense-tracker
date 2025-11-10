// commonMain/viewmodel/SettingsViewModel.kt
package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
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

    fun toggleVoiceInput(enabled: Boolean) {
        _isVoiceInputEnabled.value = enabled
    }

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun setThemeOption(option: ThemeOption) {
        _selectedThemeOption.value = option
    }
}