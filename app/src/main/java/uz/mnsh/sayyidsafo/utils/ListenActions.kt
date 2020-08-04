package uz.mnsh.sayyidsafo.utils

import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel


interface ListenActions {
    val listAudios: ArrayList<String>

    fun addChosen(unitAudioModel: UnitAudioModel)

    fun deleteChosen(id: Int)

    fun playPause()

    fun isPause(): Boolean
}