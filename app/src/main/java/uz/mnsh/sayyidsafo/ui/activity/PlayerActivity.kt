
package uz.mnsh.sayyidsafo.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_player.*
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.playback.MusicNotificationManager
import uz.mnsh.sayyidsafo.playback.MusicService
import uz.mnsh.sayyidsafo.playback.PlaybackInfoListener
import uz.mnsh.sayyidsafo.playback.PlayerAdapter
import uz.mnsh.sayyidsafo.ui.activity.MainActivity.Companion.playerAdapter
import uz.mnsh.sayyidsafo.utils.AboutUsDialog
import uz.mnsh.sayyidsafo.utils.Utils
import java.io.File

class PlayerActivity : AppCompatActivity() {

    private var seekBar: SeekBar? = null
    private var playPause: AppCompatImageView? = null
    private var next: AppCompatImageView? = null
    private var previous: AppCompatImageView? = null
    private var imgAuthor: AppCompatImageView? = null
    private var songImg: CircleImageView? = null
    private var songName: AppCompatTextView? = null
    private var tvStartTime: AppCompatTextView? = null
    private var tvEndTime: AppCompatTextView? = null
    private var mMusicService: MusicService? = null
    private var mIsBound: Boolean? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mUserIsSeeking = false
    private var mPlaybackListener: PlaybackListener? = null
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private var listAudiosFile: ArrayList<SongModel> = ArrayList()
    private var songModel: SongModel? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            mMusicService = null
        }

        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            mMusicService = (iBinder as MusicService.LocalBinder).instance
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            playerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager

            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            }
            if (mPlayerAdapter != null && mPlayerAdapter?.getCurrentSong()?.name == songModel?.name) {
                Log.d("BAG", "status")
                restorePlayerStatus()
            } else {
                onSongSelected(songModel!!)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playPause = findViewById(R.id.img_play)
        next = findViewById(R.id.img_next)
        previous = findViewById(R.id.img_previous)
        seekBar = findViewById(R.id.seek_bar)
        songName = findViewById(R.id.tv_name)
        songImg = findViewById(R.id.song_img)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvEndTime = findViewById(R.id.tv_end_time)
        imgAuthor = findViewById(R.id.img_circle)

        val model: UnitAudioModel = Gson().fromJson(
            intent.getStringExtra("model"),
            UnitAudioModel::class.java
        )

        songModel = SongModel(
            name = model.name,
            songPath = App.DIR_PATH + model.topic_id + "/" + model.getFileName()
        )

        listAudiosFile.clear()

        if (intent.getParcelableArrayListExtra<UnitAudioModel>("all") != null) {
            val list = intent.getParcelableArrayListExtra<UnitAudioModel>("all") ?: ArrayList()
            File(App.DIR_PATH ).walkTopDown().forEach { file ->
                if (file.name.endsWith(".mp3")) {
                    list.forEach {
                        if (it.getFileName() == file.name){
                            val sm = SongModel(
                                name = it.name,
                                songPath = file.path
                            )
                            listAudiosFile.add(sm)
                        }
                    }
                    Log.d("BAG", listAudiosFile.toString())
                }
            }
        } else {
            File(App.DIR_PATH + "${model.topic_id}/").walkTopDown().forEach { file ->
                if (file.name.endsWith(".mp3")) {
                    val sm = SongModel(
                        name = file.name.substring(0, file.name.length - 4),
                        songPath = file.path
                    )
                    listAudiosFile.add(sm)
                }
            }
        }

        initializeSeekBar()
        bindUI(model.topic_id.toInt(), model.duration)
    }

    private fun restorePlayerStatus() {
        seekBar!!.isEnabled = mPlayerAdapter!!.isMediaPlayer()

        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {

            mPlayerAdapter!!.onResumeActivity()
            updatePlayingInfo(restore = true, startPlay = false)
        }
    }

    private fun initializeSeekBar() {
        seekBar!!.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    mUserIsSeeking = true
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                    if (fromUser) {
                        userSelectedPosition = progress
                    }

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mUserIsSeeking = false
                    mPlayerAdapter!!.seekTo(userSelectedPosition)
                }
            })
    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {
            mPlayerAdapter!!.onPauseActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        doBindService()
    }

    private fun doUnbindService() {
        if (mIsBound!!) {
            unbindService(mConnection)
            mIsBound = false
        }
    }

    private fun doBindService() {
        bindService(
            Intent(
                this,
                MusicService::class.java
            ), mConnection, Context.BIND_AUTO_CREATE
        )
        mIsBound = true

        val startNotStickyIntent = Intent(this, MusicService::class.java)
        startService(startNotStickyIntent)
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {

        if (startPlay) {
            mPlayerAdapter!!.getMediaPlayer()?.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(
                    MusicNotificationManager.NOTIFICATION_ID,
                    mMusicNotificationManager!!.createNotification()
                )
            }, 200)
        }

        val selectedSong = mPlayerAdapter!!.getCurrentSong()

        songName?.text = selectedSong?.name

        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(selectedSong?.songPath)

        val duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        seekBar?.max = duration!!.toInt()
        metaRetriever.release()
        songImg?.setImageBitmap(Utils.songArt(selectedSong!!.songPath, this))

        if (restore) {
            seekBar!!.progress = mPlayerAdapter!!.getPlayerPosition()
            updatePlayingStatus()

            Handler().postDelayed({
                if (mMusicService!!.isRestoredFromPause) {
                    mMusicService!!.stopForeground(false)
                    mMusicService!!.musicNotificationManager!!.notificationManager
                        .notify(
                            MusicNotificationManager.NOTIFICATION_ID,
                            mMusicService!!.musicNotificationManager!!.notificationBuilder!!.build()
                        )
                    mMusicService!!.isRestoredFromPause = false
                }
            }, 200)
        }
    }

    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter!!.getState() != PlaybackInfoListener.State.PAUSED)
            R.drawable.stop
        else
            R.drawable.play
        playPause!!.post { playPause!!.setImageResource(drawable) }
    }

    @SuppressLint("SetTextI18n")
    private fun bindUI(topicID: Int, time: Long) {
        songName?.text = songModel!!.name
        songImg?.setImageBitmap(Utils.songArt(songModel!!.songPath, this))
        tvStartTime?.text = "00:00"
        tvEndTime?.text = getFormattedTime(time)

        previous?.setOnClickListener {
            if (checkIsPlayer()) {
                mPlayerAdapter!!.instantReset()
            }
        }

        next?.setOnClickListener {
            if (checkIsPlayer()) {
                mPlayerAdapter!!.skip(true)
            }
        }

        playPause?.setOnClickListener {
            resumeOrPause()
        }

        img_back.setOnClickListener {
            onBackPressed()
        }

        imgAuthor?.setOnClickListener {
            AboutUsDialog().show(supportFragmentManager, "ABOUT_US")
        }

        when (topicID) {
            1 -> tv_title.setText(R.string.text_dars1)
            2 -> tv_title.setText(R.string.text_dars2)
            3 -> tv_title.setText(R.string.text_dars3)
            4 -> tv_title.setText(R.string.text_dars4)
            5 -> tv_title.setText(R.string.text_dars5)
            6 -> tv_title.setText(R.string.text_dars6)
        }

    }

    private fun onSongSelected(song: SongModel) {
        if (!seekBar!!.isEnabled) {
            seekBar!!.isEnabled = true
        }
        try {
            mPlayerAdapter!!.setCurrentSong(song, listAudiosFile)
            mPlayerAdapter!!.initMediaPlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.resumeOrPause()
        } else {
            if (listAudiosFile.isNotEmpty()) {
                onSongSelected(songModel!!)
            }
        }
    }

    private fun checkIsPlayer(): Boolean {
        return mPlayerAdapter!!.isMediaPlayer()
    }

    private fun getFormattedTime(seconds: Long): String {
        val minutes = seconds / 60
        return String.format("%d:%02d", minutes, seconds % 60)
    }

    internal inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            if (!mUserIsSeeking) {
                seekBar!!.progress = position
                tvStartTime?.text = getFormattedTime((position / 1000).toLong())
            }
        }

        override fun onStateChanged(@State state: Int) {

            updatePlayingStatus()
            if (mPlayerAdapter!!.getState() != State.PAUSED
                && mPlayerAdapter!!.getState() != State.PAUSED
            ) {
                updatePlayingInfo(restore = false, startPlay = true)
            }
        }
    }

}