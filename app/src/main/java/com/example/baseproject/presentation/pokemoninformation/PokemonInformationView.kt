package com.example.baseproject.presentation.pokemoninformation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.baseproject.R
import com.example.baseproject.presentation.common.FlowContainerFragment
import com.example.baseproject.presentation.common.MainApplication
import com.example.baseproject.presentation.common.clicks
import com.example.baseproject.presentation.common.scene.SceneView
import com.example.domain.model.PokemonInformation
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_caught_pokemon_list_view.*
import kotlinx.android.synthetic.main.fragment_pokemon_information_view.*
import kotlinx.android.synthetic.main.fragment_pokemon_information_view.errorLayout
import kotlinx.android.synthetic.main.fragment_pokemon_information_view.toolbar
import kotlinx.android.synthetic.main.frament_pokemon_list.*
import kotlinx.android.synthetic.main.toolbar_view.*
import kotlinx.android.synthetic.main.view_empty_state.*
import java.util.*
import javax.inject.Inject


class PokemonInformationView : SceneView(), PokemonInformationUi {

    companion object {
        fun newInstance(pokemonName: String): PokemonInformationView =
            PokemonInformationView().apply {
                this.pokemonName = pokemonName
            }
    }

    override val onTryAgain: PublishSubject<Unit> = PublishSubject.create<Unit>()
    override val onReceivedPokemonName: PublishSubject<String> = PublishSubject.create<String>()
    override val onCatchPokemon: PublishSubject<String> = PublishSubject.create<String>()
    override val onReleasePokemon: PublishSubject<String> = PublishSubject.create<String>()
    private var caughtPokemon: Boolean = false

    @Inject
    lateinit var presenter: PokemonInformationPresenter

    private val component: PokemonInformationComponent? by lazy {
        DaggerPokemonInformationComponent
            .builder()
            .pokemonInformationModule(PokemonInformationModule(this))
            .applicationComponent((activity?.application as? MainApplication)?.applicationComponent)
            .flowContainerComponent((parentFragment as? FlowContainerFragment)?.component)
            .build()
    }

    private lateinit var pokemonName: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pokemon_information_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarTitleText.text = this.pokemonName.toUpperCase(Locale.ROOT)
        setupAppBar(toolbar as Toolbar, true, isModal = false)

        onViewCreated.onNext(Unit)
        onReceivedPokemonName.onNext(pokemonName)
    }

    override fun displayPokemonInformation(pokemonInformation: PokemonInformation) {
        val pokemonName = pokemonInformation.name
        caughtPokemon = pokemonInformation.caughtPokemon
        pokemonInformationNameText.text = pokemonName.toUpperCase(Locale.ROOT)

        changeCatchButtonText(caughtPokemon)

        pokemonInformation.frontSprite?.let { frontSprite ->
            Glide.with(this)
                .load(frontSprite)
                .into(frontImage)
        }

        catchPokemonButton.clicks().doOnNext {
            if (caughtPokemon) {
                onReleasePokemon.onNext(pokemonName)
            } else {
                onCatchPokemon.onNext(pokemonName)
            }
            caughtPokemon = !caughtPokemon
            changeCatchButtonText(caughtPokemon)
        }.subscribe().addTo(disposables)

        dismissBlockingError()
    }

    override fun displayBlockingError() {
        displayBlockingError(pokemonInformationContentLayout, errorLayout)
        actionButton.clicks().subscribe(onTryAgain)
    }

    private fun dismissBlockingError() {
        dismissBlockingError(pokemonInformationContentLayout, errorLayout)
    }

    private fun changeCatchButtonText(caughtPokemon: Boolean) {
        if (caughtPokemon) {
            catchPokemonButton.text = getText(R.string.pokemon_list_release_text)
        } else {
            catchPokemonButton.text = getText(R.string.pokemon_list_catch_text)
        }
    }
}