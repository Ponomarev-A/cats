package ru.ponomarev.cats.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.ponomarev.cats.MainApp
import ru.ponomarev.cats.di.qualifiers.AppContext

@Module(
    includes = [
        NetworkModule::class,
        LocalModule::class
    ]
)
class AppModule {

    @Provides
    @AppContext
    fun appContext(app: MainApp): Context = app.applicationContext
}