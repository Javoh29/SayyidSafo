package uz.mnsh.sayyidsafo.ui.fragment

import androidx.lifecycle.ViewModel
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
}