package ru.ponomarev.cats.di.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.ponomarev.cats.BuildConfig
import ru.ponomarev.cats.data.network.CatsApi
import ru.ponomarev.cats.data.network.NetworkRepository
import ru.ponomarev.cats.data.network.NetworkRepositoryImpl
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
abstract class NetworkModule {

    @Module
    companion object {

        private const val BASE_API_URL = "https://api.thecatapi.com"
        private const val AUTH_HEADER = "x-api-key"
        private const val DEFAULT_TIMEOUT_SEC = 45L

        object CatsAuthenticator : Interceptor {

            override fun intercept(chain: Interceptor.Chain): Response =
                chain.request()
                    .newBuilder()
                    .header(AUTH_HEADER, BuildConfig.CATS_API_KEY)
                    .build()
                    .let(chain::proceed)
        }

        @Provides
        @JvmStatic
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttpClient)
            .build()

        @Provides
        @JvmStatic
        fun provideHttpLoggingInterceptor(): Interceptor =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

        @Provides
        @JvmStatic
        fun provideOkHttpClient(loggingInterceptor: Interceptor): OkHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(CatsAuthenticator)
                .connectTimeout(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

        @Provides
        @JvmStatic
        fun provideCatsApi(retrofit: Retrofit): CatsApi = retrofit.create(CatsApi::class.java)
    }

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(impl: NetworkRepositoryImpl): NetworkRepository
}
