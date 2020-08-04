package uz.mnsh.sayyidsafo.ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository

class ChosenViewModelFactory(
    private val audiosRepository: AudiosRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChosenViewModel(audiosRepository) as T
    }
}