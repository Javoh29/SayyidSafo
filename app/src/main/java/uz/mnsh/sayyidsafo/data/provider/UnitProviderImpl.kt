package uz.mnsh.sayyidsafo.data.provider

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException
import java.net.InetAddress

class UnitProviderImpl(private val context: Context) : PreferenceProvider(context), UnitProvider {

    private val audioSaved = "AUDIO_SAVED"
    private val audioTime = "AUDIO_TIME"
    private val sumLessons = "SUM_LESSONS_"

    override fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            try {
                return !InetAddress.getByName("google.com").equals("")
            } catch (e: IOException) {
                false
            }
        }else false
    }

    override fun getSavedAudio(): String {
        return preferences.getString(audioSaved, "not")!!
    }

    override fun setSavedAudio(audio: String) {
        preferences.edit().putString(audioSaved, audio).apply()
    }

    override fun getSavedTime(): String {
        return preferences.getString(audioTime, "not")!!
    }

    override fun setSavedTime(time: String) {
        preferences.edit().putString(audioTime, time).apply()
    }

    override fun getSavedSum(id: Int): Int {
        return preferences.getInt("$sumLessons$id", 0)
    }

    override fun setSavedSum(id: Int, sum: Int) {
        preferences.edit().putInt("$sumLessons$id", sum).apply()
    }

}