package uz.mnsh.sayyidsafo.playback

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import uz.mnsh.sayyidsafo.data.model.SongModel

import uz.mnsh.sayyidsafo.playback.PlaybackInfoListener.*

interface PlayerAdapter {

    fun isMediaPlayer(): Boolean

    fun isPlaying(): Boolean

    fun isReset(): Boolean

    fun getCurrentSong(): SongModel?

    @State
    fun getState(): Int

    fun getPlayerPosition(): Int

    fun getMediaPlayer(): MediaPlayer?

    fun initMediaPlayer()

    fun release()

    fun resumeOrPause()

    fun reset()

    fun instantReset()

    fun skip(isNext: Boolean)

    fun seekTo(position: Int)

    fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener)

    fun registerNotificationActionsReceiver(isRegister: Boolean)


    fun setCurrentSong(song: SongModel, songs: List<SongModel>)

    fun onPauseActivity()

    fun onResumeActivity()

    fun getCurrentTitle(): LiveData<String>
}
