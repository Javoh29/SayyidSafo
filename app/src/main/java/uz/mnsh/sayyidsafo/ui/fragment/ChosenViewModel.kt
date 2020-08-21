package uz.mnsh.sayyidsafo.ui.fragment

import androidx.lifecycle.ViewModel
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository
import uz.mnsh.sayyidsafo.utils.lazyDeferred

class ChosenViewModel(
    private val audiosRepository: AudiosRepository
) : ViewModel() {

    fun getChosen() = lazyDeferred {
        audiosRepository.getChosen()
    }

    fun deleteChosen(id: Int){
        audiosRepository.deleteChosen(id)
    }

    fun setChosen(chosenModel: ChosenModel){
        audiosRepository.addChosen(chosenModel)
    }

    suspend fun getAudioForName(name: String): UnitAudioModel {
        return audiosRepository.getAudioForID(name)
    }
}