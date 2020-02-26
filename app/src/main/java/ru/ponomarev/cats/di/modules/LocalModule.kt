package ru.ponomarev.cats.di.modules

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.ponomarev.cats.data.local.CatsDatabase
import ru.ponomarev.cats.data.local.LocalRepository
import ru.ponomarev.cats.data.local.LocalRepositoryImpl
import ru.ponomarev.cats.di.qualifiers.AppContext
import javax.inject.Singleton

@Module
abstract class LocalModule {

    @Module
    companion object {

        @Provides
        fun favoritesDao(@AppContext context: Context) = CatsDatabase.getInstance(context).catsDao()
    }

    @Singleton
    @Binds
    abstract fun bindLocalRepository(impl: LocalRepositoryImpl): LocalRepository
}