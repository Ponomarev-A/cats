package ru.ponomarev.cats.ui.main

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.ponomarev.cats.domain.CatsInteractor
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor(
    private val catsInteractor: CatsInteractor
) : MvpPresenter<MainView>() {

    var isCatsLoading: Boolean = false

    private val compositeDisposable = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadCats()
        showCats(onlyFavorites = false)
    }

    fun loadCats() {
        catsInteractor
            .loadCats()
            .doOnSubscribe { isCatsLoading = true }
            .doAfterTerminate { isCatsLoading = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ /* ignore */ }, ::logError)
            .also { compositeDisposable.add(it) }
    }

    fun showCats(onlyFavorites: Boolean) {
        if (onlyFavorites) {
            catsInteractor.showFavoriteCats()
        } else {
            catsInteractor.showCats()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::updateCats, ::logError)
            .also { compositeDisposable.add(it) }
    }

    fun canLoadCats(): Boolean = catsInteractor.canLoadCats()

    fun downloadCat(url: String) {
        catsInteractor
            .downloadCat(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ /* ignore */ }, ::logError)
            .also { compositeDisposable.add(it) }
    }

    fun setFavoriteCat(cat: CatVO) {
        catsInteractor
            .setFavorite(cat)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ /* ignore*/ }, ::logError)
            .also { compositeDisposable.add(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun logError(throwable: Throwable) {
        Log.e(MainPresenter::class.java.simpleName, throwable.toString(), throwable)
    }
}
