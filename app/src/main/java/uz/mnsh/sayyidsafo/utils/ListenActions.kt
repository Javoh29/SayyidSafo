package uz.mnsh.sayyidsafo.utils

import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.model.SongModel


interface ListenActions {
    val listAudios: ArrayList<String>

    fun addChosen(unitAudioModel: UnitAudioModel)

    fun deleteChosen(id: Int)

    fun itemPlay(model: SongModel)
}