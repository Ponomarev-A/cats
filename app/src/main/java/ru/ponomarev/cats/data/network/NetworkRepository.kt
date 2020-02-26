package ru.ponomarev.cats.data.network

import io.reactivex.Single
import ru.ponomarev.cats.ui.main.CatVO
import java.io.File
import javax.inject.Inject


interface NetworkRepository {
    fun loadImages(): Single<List<CatVO>>
    fun isLastPage(): Boolean
    fun downloadImage(url: String): Single<File>
}

class NetworkRepositoryImpl @Inject constructor(
    private val catsApi: CatsApi,
    private val downloadManagerHelper: DownloadManagerHelper
) : NetworkRepository {

    companion object {
        private const val IMAGES_LIMIT = 10
        private const val ORDER_DESC = "desc"
        private const val HEADER_PAGE_COUNT = "pagination-count"
    }

    private var availablePageCount: Int = 0
    private var nextPage: Int = 0

    override fun loadImages(): Single<List<CatVO>> =
        catsApi
            .searchCats(page = getNextPage(), limit = IMAGES_LIMIT, order = ORDER_DESC)
            .map { response ->
                with(response) {
                    if (isSuccessful) {
                        availablePageCount = headers()[HEADER_PAGE_COUNT]?.toInt() ?: 0
                        body()?.map { dto ->
                            CatVO(
                                id = dto.id,
                                url = dto.url,
                                isFavorite = false
                            )
                        }
                    } else {
                        throw IllegalStateException(errorBody().toString())
                    }
                }
            }

    override fun isLastPage(): Boolean = nextPage == availablePageCount

    private fun getNextPage(): Int =
        if (nextPage <= availablePageCount)
            nextPage++
        else
            availablePageCount

    override fun downloadImage(url: String): Single<File> =
        downloadManagerHelper.downloadFile(url)
}