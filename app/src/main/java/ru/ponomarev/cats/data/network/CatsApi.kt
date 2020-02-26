package ru.ponomarev.cats.data.network

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CatsApi {

    @GET("v1/images/search")
    fun searchCats(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("order") order: String
    ): Single<Response<List<CatDTO>>>
}

data class CatDTO(
    val id: String,
    val url: String
)