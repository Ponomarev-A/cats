package ru.ponomarev.cats

import android.content.Context
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import ru.ponomarev.cats.di.components.AppComponent
import ru.ponomarev.cats.di.components.DaggerAppComponent

class MainApp : DaggerApplication() {

    companion object {
        private lateinit var component: AppComponent

        fun getComponent(): AppComponent = component
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        component = DaggerAppComponent
            .builder()
            .application(this)
            .build()
        return component
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}