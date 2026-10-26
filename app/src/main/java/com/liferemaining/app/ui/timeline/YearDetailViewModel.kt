package com.liferemaining.app.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.model.YearNote
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class YearDetailViewModel(
    private val container: AppContainer,
    private val year: Int
) : ViewModel() {

    val notes: StateFlow<List<YearNote>> = container.yearNoteRepository
        .observeYear(year)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ageAtYear: StateFlow<Int?> = container.profileStore.profile
        .map { profile -> profile.birthDate?.let { year - it.year } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addNote(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { container.yearNoteRepository.add(year, content.trim()) }
    }

    fun deleteNote(note: YearNote) {
        viewModelScope.launch { container.yearNoteRepository.delete(note) }
    }
}
