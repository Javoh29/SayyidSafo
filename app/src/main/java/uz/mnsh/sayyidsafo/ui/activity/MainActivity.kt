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
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.merge_card_mode.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.data.provider.UnitProvider
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository
import uz.mnsh.sayyidsafo.playback.MusicNotificationManager
import uz.mnsh.sayyidsafo.playback.MusicService
import uz.mnsh.sayyidsafo.playback.PlaybackInfoListener
import uz.mnsh.sayyidsafo.playback.PlayerAdapter
import uz.mnsh.sayyidsafo.utils.AboutUsDialog
import java.lang.NullPointerException


class MainActivity : AppCompatActivity(), KodeinAware, NavigationView.OnNavigationItemSelectedListener {

    override val kodein by kodein()
    private val audiosRepository: AudiosRepository by instance<AudiosRepository>()
    private val unitProvider: UnitProvider by instance<UnitProvider>()
    private lateinit var navController: NavController
    private lateinit var navView: BottomNavigationView
    var drawerLayout: DrawerLayout? = null

    private lateinit var seekBar: SeekBar
    private lateinit var playPause: AppCompatImageView
    private lateinit var next: AppCompatImageView
    private lateinit var previous: AppCompatImageView
    private lateinit var imgAuthor: AppCompatImageView
    private lateinit var songImg: CircleImageView
    private lateinit var songName: AppCompatTextView
    private lateinit var tvStartTime: AppCompatTextView
    private lateinit var tvEndTime: AppCompatTextView
    private lateinit var tvTitleBottom: AppCompatTextView
    private lateinit var tvTitle: AppCompatTextView
    private lateinit var tvTitleCar: AppCompatTextView
    private lateinit var imgPlayCar: AppCompatImageView
    private lateinit var imgReplayBack: AppCompatImageView
    private lateinit var imgReplayNext: AppCompatImageView
    private var mMusicService: MusicService? = null
    private var mIsBound: Boolean? = null
    private var mUserIsSeeking = false
    private var mPlaybackListener: PlaybackListener? = null
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private var songModel: SongModel? = null
    private lateinit var bottomDialog: BottomSheetDialog

    companion object {
        var playerAdapter: PlayerAdapter? = null
        var isSongPlay: MutableLiveData<Boolean> = MutableLiveData()
        var pageIndex: Int = 1
    }

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
                if (songModel != null){
                    onSongSelected(songModel!!)
                }
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
        tvTitle = findViewById(R.id.tvName)
        findViewById<NavigationView>(R.id.navigation_menu).setNavigationItemSelectedListener(this)

        tvTitleCar = findViewById(R.id.tv_title)
        imgPlayCar = findViewById(R.id.img_play_car)
        imgReplayBack = findViewById(R.id.img_replay_back)
        imgReplayNext = findViewById(R.id.img_replay_next)

        bottomDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_player, findViewById(R.id.bottomSheetContainer))
        bottomDialog.setContentView(bottomSheetView)

        playPause = bottomSheetView.findViewById(R.id.img_play)
        next = bottomSheetView.findViewById(R.id.img_next)
        previous = bottomSheetView.findViewById(R.id.img_previous)
        seekBar = bottomSheetView.findViewById(R.id.seekBar)
        songName = bottomSheetView.findViewById(R.id.tv_name_bottom)
        songImg = bottomSheetView.findViewById(R.id.song_img)
        tvStartTime = bottomSheetView.findViewById(R.id.tv_start_time)
        tvEndTime = bottomSheetView.findViewById(R.id.tv_end_time)
        imgAuthor = bottomSheetView.findViewById(R.id.img_circle)
        tvTitleBottom = bottomSheetView.findViewById(R.id.tv_title)

        GlobalScope.launch(Dispatchers.IO) {
            if (unitProvider.isOnline()) {
                audiosRepository.fetchAudios()
            } else {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Internet bilan aloqa yoq", Toast.LENGTH_LONG).show()
                }
            }
        }
        requestPermissions()

        if (unitProvider.getSavedAudio().isNotEmpty()){
            songModel = Gson().fromJson(
                unitProvider.getSavedAudio(),
                SongModel::class.java
            )

            if (songModel != null) {
                val metaRetriever = MediaMetadataRetriever()
                metaRetriever.setDataSource(songModel?.songPath)
                bindUI(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong())
            } else bindUI(0L)

        }else{
            bindUI(0L)
        }

        layoutPlayer.setOnClickListener {
            bottomDialog.show()
        }

        initializeSeekBar()

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
        when {
            drawerLayout!!.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout!!.closeDrawer(GravityCompat.START)
            }
            frameCar.visibility == View.VISIBLE -> {
                frameCar.visibility = View.GONE
            }
            else -> super.onBackPressed()
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
        seekBar.isEnabled = playerAdapter!!.isMediaPlayer()

        if (playerAdapter != null && playerAdapter!!.isMediaPlayer()) {

            playerAdapter!!.onResumeActivity()
            updatePlayingInfo(restore = true, startPlay = false)
        }
    }

    private fun initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(
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

    override fun onStop() {
        try {
            unitProvider.setSavedAudio(Gson().toJson(playerAdapter?.getCurrentSong()))
        }catch (e: NullPointerException){}
        super.onStop()
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

        songName.text = selectedSong?.name
        tvTitleCar.text = selectedSong?.name

        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(selectedSong?.songPath)

        val duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        seekBar.max = duration!!.toInt()
        progressBar.max = duration.toInt()
        tvEndTime.text = getFormattedTime(duration.toInt()/1000L)
        metaRetriever.release()
        tvTitle.text = selectedSong?.name

        setImageSong(selectedSong!!.topicID)

        if (restore) {
            seekBar.progress = playerAdapter!!.getPlayerPosition()
            progressBar.progress = playerAdapter!!.getPlayerPosition()
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
        playPause.post { playPause.setImageResource(drawable) }
        imgPlay!!.post { imgPlay!!.setImageResource(drawable) }
        imgPlayCar.post { imgPlayCar.setImageResource(drawable) }
    }

    @SuppressLint("SetTextI18n")
    private fun bindUI(time: Long) {
        if (songModel != null){
            songName.text = songModel!!.name
            tvTitleCar.text = songModel!!.name
            setImageSong(songModel!!.topicID)
        }
        tvStartTime.text = "00:00"
        tvEndTime.text = getFormattedTime(time/1000)

        previous.setOnClickListener {
            if (checkIsPlayer()) {
                playerAdapter!!.instantReset()
            }
        }

        next.setOnClickListener {
            if (checkIsPlayer()) {
                playerAdapter!!.skip(true)
            }
        }

        imgPlay.setOnClickListener {
            if (checkIsPlayer()){
                playerAdapter!!.resumeOrPause()
            }else if (playerAdapter!!.getMediaPlayer() == null){
                if (songModel != null){
                    playerAdapter?.initMediaPlayer()
                }
            }
        }

        imgPlayCar.setOnClickListener {
            if (checkIsPlayer()){
                playerAdapter!!.resumeOrPause()
            }else if (playerAdapter!!.getMediaPlayer() == null){
                if (songModel != null){
                    playerAdapter?.initMediaPlayer()
                }
            }
        }

        playPause.setOnClickListener {
            if (checkIsPlayer()){
                playerAdapter!!.resumeOrPause()
            }else if (playerAdapter!!.getMediaPlayer() == null){
                if (songModel != null){
                    playerAdapter?.initMediaPlayer()
                }
            }
        }

        imgAuthor.setOnClickListener {
            bottomDialog.dismiss()
            frameCar.visibility = View.VISIBLE
        }

        imgReplayBack.setOnClickListener {
            if (playerAdapter != null && playerAdapter!!.isPlaying()) {
                if (playerAdapter!!.getMediaPlayer()!!.currentPosition.minus(30000) > 0) {
                    playerAdapter!!.seekTo(
                        playerAdapter!!.getMediaPlayer()!!.currentPosition.minus(
                            30000
                        )
                    )
                } else {
                    playerAdapter!!.seekTo(0)
                }
            }
        }

        imgReplayNext.setOnClickListener {
            if (playerAdapter != null && playerAdapter!!.isPlaying()) {
                if (playerAdapter!!.getMediaPlayer()!!.currentPosition.plus(30000) < playerAdapter!!.getMediaPlayer()!!.duration) {
                    playerAdapter!!.seekTo(playerAdapter!!.getMediaPlayer()!!.currentPosition.plus(30000))
                }else{
                    playerAdapter!!.skip(true)
                }
            }
        }

    }

    private fun onSongSelected(song: SongModel) {
        if (!seekBar.isEnabled) {
            seekBar.isEnabled = true
        }
        try {
            playerAdapter!!.setCurrentSong(song, null)
            tvTitle.text = song.name
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setImageSong(id: String){
        when (id) {
            "1" -> {
                tvTitleBottom.setText(R.string.text_dars1)
                songImg.setImageResource(R.drawable.img_song_1)
                imgSong.setImageResource(R.drawable.img_song_1)
                pageIndex = 1
            }
            "2" -> {
                tvTitleBottom.setText(R.string.text_dars2)
                songImg.setImageResource(R.drawable.img_song_2)
                imgSong.setImageResource(R.drawable.img_song_2)
                pageIndex = 2
            }
            "3" -> {
                tvTitleBottom.setText(R.string.text_dars3)
                songImg.setImageResource(R.drawable.img_song_3)
                imgSong.setImageResource(R.drawable.img_song_3)
                pageIndex = 3
            }
            "4" -> {
                tvTitleBottom.setText(R.string.text_dars4)
                songImg.setImageResource(R.drawable.img_song_4)
                imgSong.setImageResource(R.drawable.img_song_4)
                pageIndex = 4
            }
            "5" -> {
                tvTitleBottom.setText(R.string.text_dars5)
                songImg.setImageResource(R.drawable.img_song_5)
                imgSong.setImageResource(R.drawable.img_song_5)
                pageIndex = 5
            }
            "6" -> {
                tvTitleBottom.setText(R.string.text_dars6)
                songImg.setImageResource(R.drawable.img_song_6)
                imgSong.setImageResource(R.drawable.img_song_6)
                pageIndex = 6
            }
            "7" -> {
                tvTitleBottom.setText(R.string.text_dars7)
                songImg.setImageResource(R.drawable.img_song_7)
                imgSong.setImageResource(R.drawable.img_song_7)
                pageIndex = 7
            }
            "8" -> {
                tvTitleBottom.setText(R.string.text_dars8)
                songImg.setImageResource(R.drawable.img_song_8)
                imgSong.setImageResource(R.drawable.img_song_8)
                pageIndex = 8
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
                seekBar.progress = position
                seekBar.isEnabled = true
                progressBar.progress = position
                tvStartTime.text = getFormattedTime((position / 1000).toLong())
            }
        }

        override fun onStateChanged(@State state: Int) {

            updatePlayingStatus()
            if (playerAdapter!!.getState() != State.PAUSED
                && playerAdapter!!.getState() != State.PAUSED
            ) {
                isSongPlay.postValue(true)
                updatePlayingInfo(restore = false, startPlay = true)
            }else isSongPlay.postValue(false)
        }
    }
}