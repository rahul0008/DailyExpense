package com.example.dailyexpense.ui.presenter.expenseEntry

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
}