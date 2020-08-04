package uz.mnsh.sayyidsafo.ui.fragment

import androidx.lifecycle.ViewModel
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository
import uz.mnsh.sayyidsafo.utils.lazyDeferred

class ListenViewModel(
    private val audiosRepository: AudiosRepository
) : ViewModel() {

    fun getAudios(id: Int) = lazyDeferred {
        audiosRepository.getAudios(id)
    }

    fun getChosen() = lazyDeferred {
        audiosRepository.getChosen()
    }

    fun setChosen(chosenModel: ChosenModel){
        audiosRepository.addChosen(chosenModel)
    }

    fun deleteChosen(id: Int){
        audiosRepository.deleteChosen(id)
    }

}