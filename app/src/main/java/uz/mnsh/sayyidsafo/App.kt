package uz.mnsh.sayyidsafo

import android.app.Application
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import uz.mnsh.sayyidsafo.data.db.AudiosDatabase
import uz.mnsh.sayyidsafo.data.network.ApiService
import uz.mnsh.sayyidsafo.data.network.*
import uz.mnsh.sayyidsafo.data.provider.UnitProvider
import uz.mnsh.sayyidsafo.data.provider.UnitProviderImpl
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository
import uz.mnsh.sayyidsafo.data.repository.AudiosRepositoryImpl
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import uz.mnsh.sayyidsafo.ui.fragment.ChosenViewModelFactory
import uz.mnsh.sayyidsafo.ui.fragment.ListenViewModelFactory

class App: Application(), KodeinAware {

    override val kodein: Kodein
        get() = Kodein.lazy {
            import(androidXModule(this@App))

            bind() from singleton { AudiosDatabase(instance()) }
            bind() from singleton { instance<AudiosDatabase>().audiosDao() }
            bind<UnitProvider>() with singleton { UnitProviderImpl(instance()) }
            bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }
            bind() from singleton { ApiService() }
            bind<AudiosRepository>() with singleton { AudiosRepositoryImpl(instance(), instance(), instance()) }
            bind() from provider { ListenViewModelFactory(instance()) }
            bind() from provider { ChosenViewModelFactory(instance()) }
        }

    companion object {
        const val BASE_URL = "http://5.182.26.44:8080/api/"
        var DIR_PATH = ""
    }

    override fun onCreate() {
        super.onCreate()
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)
    }

}