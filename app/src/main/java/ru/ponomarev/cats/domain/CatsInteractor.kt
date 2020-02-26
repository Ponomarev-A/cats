package ru.ponomarev.cats.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.ponomarev.cats.data.local.LocalRepository
import ru.ponomarev.cats.data.network.NetworkRepository
import ru.ponomarev.cats.ui.main.CatVO
import java.io.File
import javax.inject.Inject

class CatsInteractor @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val localRepository: LocalRepository
) {
    fun loadCats(): Completable =
        networkRepository
            .loadImages()
            .flatMapCompletable(localRepository::addCats)

    fun canLoadCats(): Boolean =
        networkRepository.isLastPage().not()

    fun showCats(): Flowable<List<CatVO>> =
        localRepository.getCats()

    fun showFavoriteCats(): Flowable<List<CatVO>> =
        localRepository.getFavoriteCats()

    fun downloadCat(url: String): Single<File> =
        networkRepository.downloadImage(url)

    fun setFavorite(cat: CatVO): Completable =
        localRepository.updateCat(cat.copy(isFavorite = cat.isFavorite.not()))
}


