package com.example.dailyexpense.ui.presenter.expenseEntry

import android.widget.Toast

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
}