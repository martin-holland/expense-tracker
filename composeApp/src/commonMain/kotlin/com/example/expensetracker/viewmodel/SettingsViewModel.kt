package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensetracker.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SettingsViewModel : ViewModel() {



    private val _selectedCurrency = MutableStateFlow(Currency.USD)

    val selectedCurrency: StateFlow<Currency> = _selectedCurrency

    private val _isVoiceInputEnabled = MutableStateFlow(false)
    val isVoiceInputEnabled: StateFlow<Boolean> = _isVoiceInputEnabled



    // Other states...

    private val _selectedThemeOption = MutableStateFlow("Light")
    val selectedThemeOption: StateFlow<String> = _selectedThemeOption

    // Functions to update state


    // Add function for voice input toggle
    fun toggleVoiceInput(enabled: Boolean) {
        _isVoiceInputEnabled.value = enabled
    }
    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }



    fun setThemeOption(option: String) {
        _selectedThemeOption.value = option
    }


}