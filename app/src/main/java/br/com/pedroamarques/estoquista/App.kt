package br.com.pedroamarques.estoquista

import android.app.Application
import br.com.pedroamarques.estoquista.factory.AppDatabase
import timber.log.Timber

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onTerminate() {
        AppDatabase.closeIfNeeded()
        super.onTerminate()
    }

}