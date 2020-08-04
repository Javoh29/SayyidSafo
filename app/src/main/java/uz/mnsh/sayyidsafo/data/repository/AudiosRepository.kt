package uz.mnsh.sayyidsafo.data.repository

import androidx.lifecycle.LiveData
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel

interface AudiosRepository {
    suspend fun getAudios(id: Int): LiveData<out List<UnitAudioModel>>

    suspend fun getChosen(): LiveData<List<UnitAudioModel>>

    fun addChosen(chosenModel: ChosenModel)

    fun deleteChosen(id: Int)

    fun fetchAudios()

}