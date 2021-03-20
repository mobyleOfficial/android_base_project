package com.example.baseproject.presentation.pokemoninformation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baseproject.presentation.common.*
import com.example.baseproject.presentation.common.scene.SceneViewModel
import com.example.domain.model.PokemonInformation
import com.example.domain.usecase.*
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class PokemonInformationViewModel @Inject constructor(
    private val getPokemonInformationUC: GetPokemonInformationUC,
    private val catchPokemonUC: CatchPokemonUC,
    private val releasePokemonUC: ReleasePokemonUC
) : SceneViewModel() {
    private val pokemonInformationMutableLiveData = MutableLiveData<StateEvent<PokemonInformation>>()
    val pokemonInformationLiveData: LiveData<StateEvent<PokemonInformation>> = pokemonInformationMutableLiveData

    private val catchPokemonMutableLiveData = MutableLiveData<StateEvent<Unit>>()
    val catchPokemonLiveData: LiveData<StateEvent<Unit>> = catchPokemonMutableLiveData

    init {
        baseEventsMutableLiveData.postValue(ViewModelLoading<Unit>())
    }

    fun getPokemonInformation(pokemonName: String) {

        viewModelScope.launch {
            getPokemonInformationUC.getFlow(GetPokemonInformationParamsUC(pokemonName))
                .onEach { pokemonInformation ->
                    pokemonInformationMutableLiveData.postValue(ViewModelSuccess(pokemonInformation))
                }.catch {
                    pokemonInformationMutableLiveData.postValue(ViewModelError(it))
                }.collect {
                    baseEventsMutableLiveData.postValue(ViewModelDismissLoading<Unit>())
                }
        }
    }

    fun catchPokemon(pokemonName: String) {
        catchPokemonUC.getCompletable(CatchPokemonParamsUC(pokemonName = pokemonName))
            .doOnComplete {
                catchPokemonMutableLiveData.postValue(ViewModelSuccess(Unit))
            }.subscribe()
            .addTo(disposables)
    }

    fun releasePokemon(pokemonName: String) {
        releasePokemonUC.getCompletable(ReleasePokemonUCParams(pokemonName = pokemonName))
            .doOnComplete {
                catchPokemonMutableLiveData.postValue(ViewModelSuccess(Unit))
            }.subscribe()
            .addTo(disposables)
    }
}