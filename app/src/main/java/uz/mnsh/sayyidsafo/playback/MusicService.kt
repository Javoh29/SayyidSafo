package uz.mnsh.sayyidsafo.playback

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.data.provider.UnitProvider
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import java.lang.NullPointerException

class MusicService : Service(), KodeinAware {

    override val kodein by kodein()
    private val unitProvider: UnitProvider by instance<UnitProvider>()

    private val mIBinder = LocalBinder()

    var mediaPlayerHolder: MediaPlayerHolder? = null
        private set

    var musicNotificationManager: MusicNotificationManager? = null
        private set

    var isRestoredFromPause = false

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        mediaPlayerHolder!!.registerNotificationActionsReceiver(false)
        musicNotificationManager = null
        mediaPlayerHolder!!.release()
        try {
            unitProvider.setSavedAudio(Gson().toJson(mediaPlayerHolder?.getCurrentSong()))
        }catch (e: NullPointerException){}
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        if (mediaPlayerHolder == null) {
            mediaPlayerHolder = MediaPlayerHolder(this)
            musicNotificationManager = MusicNotificationManager(this)
            mediaPlayerHolder!!.registerNotificationActionsReceiver(true)
        }
        return mIBinder
    }

    inner class LocalBinder : Binder() {
        val instance: MusicService
            get() = this@MusicService
    }
}
