package uz.mnsh.sayyidsafo.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_player.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository
import uz.mnsh.sayyidsafo.playback.MusicNotificationManager
import uz.mnsh.sayyidsafo.playback.MusicService
import uz.mnsh.sayyidsafo.playback.PlaybackInfoListener
import uz.mnsh.sayyidsafo.playback.PlayerAdapter
import uz.mnsh.sayyidsafo.utils.AboutUsDialog
import uz.mnsh.sayyidsafo.utils.Utils


class MainActivity : AppCompatActivity(), KodeinAware, NavigationView.OnNavigationItemSelectedListener {

    override val kodein by kodein()
    private val audiosRepository: AudiosRepository by instance<AudiosRepository>()
    private lateinit var navController: NavController
    private lateinit var navView: BottomNavigationView
    var drawerLayout: DrawerLayout? = null

    companion object {
        var playerAdapter: PlayerAdapter? = null
    }

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
            playerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager

            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                playerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            }
            if (playerAdapter != null && playerAdapter?.getCurrentSong()?.name == songModel?.name) {
                Log.d("BAG", "status")
                restorePlayerStatus()
            } else {
                onSongSelected(songModel!!)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        drawerLayout = findViewById(R.id.drawer_layout)
        findViewById<NavigationView>(R.id.navigation_menu).setNavigationItemSelectedListener(this)

        playPause = findViewById(R.id.img_play)
        next = findViewById(R.id.img_next)
        previous = findViewById(R.id.img_previous)
        seekBar = findViewById(R.id.seekBar)
        songName = findViewById(R.id.tv_name)
        songImg = findViewById(R.id.song_img)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvEndTime = findViewById(R.id.tv_end_time)
        imgAuthor = findViewById(R.id.img_circle)

        audiosRepository.fetchAudios()
        requestPermissions()

        val bottomDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_player, findViewById(R.id.bottomSheetContainer))
        bottomDialog.setContentView(bottomSheetView)

        layoutPlayer.setOnClickListener {
            bottomDialog.show()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        App.DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        App.DIR_PATH += "/SayyidSafo/"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.telegram_chanel -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.telegram_url))
                startActivity(intent)
            }
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var message = getString(R.string.about_us_text)
                message =
                    message + "\n" + getString(R.string.app_url) + "\n\n"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, "Улашиш"))
            }
            R.id.about -> {
                AboutUsDialog().show(supportFragmentManager, "ABOUT_US")
            }
            R.id.other_apps -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.our_app))
                startActivity(intent)
            }
        }

        return true
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.telegram_chanel -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.telegram_url))
                startActivity(intent)
            }
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var message = getString(R.string.about_us_text)
                message =
                    message + "\n" + getString(R.string.app_url) + "\n\n"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, "Улашиш"))
            }
            R.id.about -> {
                AboutUsDialog().show(supportFragmentManager, "ABOUT_US")
            }
            R.id.other_apps -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.our_app))
                startActivity(intent)
            }
        }
        return true
    }

    private fun restorePlayerStatus() {
        seekBar!!.isEnabled = playerAdapter!!.isMediaPlayer()

        if (playerAdapter != null && playerAdapter!!.isMediaPlayer()) {

            playerAdapter!!.onResumeActivity()
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
                    playerAdapter!!.seekTo(userSelectedPosition)
                }
            })
    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
        if (playerAdapter != null && playerAdapter!!.isMediaPlayer()) {
            playerAdapter!!.onPauseActivity()
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
            playerAdapter!!.getMediaPlayer()?.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(
                    MusicNotificationManager.NOTIFICATION_ID,
                    mMusicNotificationManager!!.createNotification()
                )
            }, 200)
        }

        val selectedSong = playerAdapter!!.getCurrentSong()

        songName?.text = selectedSong?.name

        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(selectedSong?.songPath)

        val duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        seekBar?.max = duration!!.toInt()
        metaRetriever.release()
        songImg?.setImageBitmap(Utils.songArt(selectedSong!!.songPath, this))

        if (restore) {
            seekBar!!.progress = playerAdapter!!.getPlayerPosition()
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
        val drawable = if (playerAdapter!!.getState() != PlaybackInfoListener.State.PAUSED)
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
                playerAdapter!!.instantReset()
            }
        }

        next?.setOnClickListener {
            if (checkIsPlayer()) {
                playerAdapter!!.skip(true)
            }
        }

        playPause?.setOnClickListener {
            resumeOrPause()
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
            playerAdapter!!.setCurrentSong(song, listAudiosFile)
            playerAdapter!!.initMediaPlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun resumeOrPause() {
        if (checkIsPlayer()) {
            playerAdapter!!.resumeOrPause()
        } else {
            if (listAudiosFile.isNotEmpty()) {
                onSongSelected(songModel!!)
            }
        }
    }

    private fun checkIsPlayer(): Boolean {
        return playerAdapter!!.isMediaPlayer()
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
            if (playerAdapter!!.getState() != State.PAUSED
                && playerAdapter!!.getState() != State.PAUSED
            ) {
                updatePlayingInfo(restore = false, startPlay = true)
            }
        }
    }
}