package com.tristamisland.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tristamisland.data.IslandGameStore
import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.IslandStory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IslandViewModel(
    private val story: IslandStory,
    store: IslandGameStore,
) : ViewModel() {
    private val engine = IslandEngine(story)
    private val session = IslandSession(engine, store)
    private val uiState = MutableStateFlow(IslandUi())

    val ui: StateFlow<IslandUi> = uiState.asStateFlow()

    init {
        viewModelScope.launch { show(session.restore(), IslandMode.MENU) }
    }

    fun open(mode: IslandMode) {
        if (!uiState.value.ready || uiState.value.busy) return
        uiState.value = uiState.value.copy(mode = mode, confirmNew = false, notice = null)
    }

    fun openPage(page: IslandPage) {
        if (uiState.value.busy) return
        uiState.value = uiState.value.copy(page = page, notice = null)
    }

    fun continueGame() {
        val ui = uiState.value
        if (!ui.ready || ui.busy || !ui.hasSave) return
        uiState.value = ui.copy(mode = IslandMode.PLAY, notice = null)
    }

    fun requestNewGame() {
        val ui = uiState.value
        if (!ui.ready || ui.busy) return
        if (ui.hasSave) {
            uiState.value = ui.copy(confirmNew = true)
        } else {
            startNewGame()
        }
    }

    fun dismissNewGame() {
        uiState.value = uiState.value.copy(confirmNew = false)
    }

    fun startNewGame() {
        if (uiState.value.busy) return
        uiState.value = uiState.value.copy(busy = true, confirmNew = false, page = IslandPage.SCENE)
        viewModelScope.launch {
            show(session.replace(), IslandMode.PLAY)
        }
    }

    fun choose(actionId: String) {
        if (uiState.value.busy) return
        uiState.value = uiState.value.copy(busy = true)
        viewModelScope.launch { show(session.choose(actionId), IslandMode.PLAY) }
    }

    fun travelTo(locationId: String) {
        if (uiState.value.busy) return
        uiState.value = uiState.value.copy(busy = true)
        viewModelScope.launch { show(session.travelTo(locationId), IslandMode.PLAY) }
    }

    fun acknowledgeAct() {
        if (uiState.value.busy) return
        uiState.value = uiState.value.copy(busy = true)
        viewModelScope.launch { show(session.acknowledgeAct(), IslandMode.PLAY) }
    }

    fun continuePrologue() {
        if (uiState.value.busy) return
        uiState.value = uiState.value.copy(busy = true)
        viewModelScope.launch { show(session.continuePrologue(), IslandMode.PLAY) }
    }

    private fun show(snapshot: IslandSnapshot, mode: IslandMode) {
        uiState.value = IslandUi(
            mode = mode,
            page = if (mode == IslandMode.PLAY) uiState.value.page else IslandPage.SCENE,
            ready = true,
            busy = false,
            hasSave = snapshot.record != null,
            confirmNew = false,
            notice = snapshot.notice,
            play = presentIsland(story, engine, snapshot),
        )
    }
}

class IslandViewModelFactory(
    private val story: IslandStory,
    private val store: IslandGameStore,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IslandViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IslandViewModel(story, store) as T
        }
        throw IllegalArgumentException("Unknown model: ${modelClass.name}")
    }
}
