package ru.ponomarev.cats.di.components

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import ru.ponomarev.cats.MainApp
import ru.ponomarev.cats.di.modules.AppModule
import ru.ponomarev.cats.ui.main.MainFragment
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class
    ]
)
interface AppComponent : AndroidInjector<MainApp> {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: MainApp): Builder

        fun build(): AppComponent

    }

    fun inject(mainFragment: MainFragment)
}