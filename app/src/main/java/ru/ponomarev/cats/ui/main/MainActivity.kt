package ru.ponomarev.cats.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.arellomobile.mvp.MvpAppCompatActivity
import ru.ponomarev.cats.R

class MainActivity : MvpAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}