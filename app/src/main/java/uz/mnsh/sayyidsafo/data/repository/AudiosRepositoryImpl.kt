package uz.mnsh.sayyidsafo.data.repository

import androidx.lifecycle.LiveData
import uz.mnsh.sayyidsafo.data.db.AudiosDao
import uz.mnsh.sayyidsafo.data.network.response.AudioResponse
import uz.mnsh.sayyidsafo.data.provider.UnitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.network.ApiService

class AudiosRepositoryImpl(
    private val audiosDao: AudiosDao,
    private val api: ApiService,
    private val unitProvider: UnitProvider
) : AudiosRepository {

    override suspend fun getAudios(id: Int): LiveData<out List<UnitAudioModel>> {
        return withContext(Dispatchers.IO) {
            return@withContext audiosDao.getAudios(topicId = id)
        }
    }

    override suspend fun getChosen(): LiveData<List<UnitAudioModel>> {
        return withContext(Dispatchers.IO) {
            return@withContext audiosDao.getChosen()
        }
    }

    override suspend fun getAudioForID(name: String): UnitAudioModel {
        return withContext(Dispatchers.IO) {
            return@withContext audiosDao.getAudioForID(name)
        }
    }

    override fun addChosen(chosenModel: ChosenModel) {
        GlobalScope.launch(Dispatchers.IO) {
            audiosDao.upsertChosen(chosenModel)
        }
    }

    override fun deleteChosen(id: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            audiosDao.deleteChosen(id)
        }
    }

    override fun fetchAudios() {
        GlobalScope.launch(Dispatchers.IO) {
            if (unitProvider.isOnline()) {
                try {
                    audiosDao.deleteAudios()
                    for (i in 2..9) {
                        val fetchResponse = api.getAudiosAsync(i)
                        if (fetchResponse.isSuccessful && fetchResponse.body()!!.data.isNotEmpty()) {
                            unitProvider.setSavedSum(i, fetchResponse.body()!!.data.size)
                            fetchResponse.body()!!.data.forEach {
                                audiosDao.upsertAudios(it)
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

}