package ru.ponomarev.cats.data.local

import io.reactivex.Completable
import io.reactivex.Flowable
import ru.ponomarev.cats.ui.main.CatVO
import javax.inject.Inject

interface LocalRepository {
    fun getCats(): Flowable<List<CatVO>>
    fun addCats(newCats: List<CatVO>): Completable
    fun addCat(cat: CatVO): Completable
    fun updateCat(cat: CatVO): Completable
    fun getFavoriteCats(): Flowable<List<CatVO>>
}

class LocalRepositoryImpl @Inject constructor(
    private val catsDao: CatsDao
) : LocalRepository {

    override fun getCats(): Flowable<List<CatVO>> =
        catsDao.getCats()
            .map { catEntities -> catEntities.map(CatEntity::toVo) }

    override fun getFavoriteCats(): Flowable<List<CatVO>> =
        catsDao.getFavoriteCats()
            .map { catEntities -> catEntities.map(CatEntity::toVo) }

    override fun addCats(newCats: List<CatVO>): Completable =
        catsDao.addCats(newCats.map(CatVO::toEntity))

    override fun addCat(cat: CatVO): Completable =
        catsDao.addCat(cat.toEntity())

    override fun updateCat(cat: CatVO): Completable =
        catsDao.updateCat(cat.toEntity())
}

private fun CatVO.toEntity(): CatEntity =
    CatEntity(
        id = id,
        url = url,
        isFavorite = isFavorite
    )

private fun CatEntity.toVo(): CatVO =
    CatVO(
        id = id,
        url = url,
        isFavorite = isFavorite
    )

