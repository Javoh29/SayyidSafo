package uz.mnsh.sayyidsafo.data.provider

interface UnitProvider {
    fun isOnline(): Boolean

    fun getSavedAudio(): String

    fun setSavedAudio(audio: String)

    fun getSavedTime(): String

    fun setSavedTime(time: String)

    fun getSavedSum(id: Int): Int

    fun setSavedSum(id: Int, sum: Int)
}