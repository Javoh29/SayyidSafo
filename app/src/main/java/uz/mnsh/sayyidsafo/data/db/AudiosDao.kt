package uz.mnsh.sayyidsafo.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uz.mnsh.sayyidsafo.data.db.model.AudioModel
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel

@Dao
interface AudiosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAudios(audioModel: AudioModel)

    @Query("select * from audios_table where topic_id == :topicId")
    fun getAudios(topicId: Int): LiveData<List<UnitAudioModel>>

    @Query("DELETE FROM audios_table")
    fun deleteAudios()

    @Query("DELETE FROM audios_table where topic_id == :topicId")
    fun deleteForID(topicId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertChosen(chosenModel: ChosenModel)

    @Query("select * from chosen_table")
    fun getChosen(): LiveData<List<UnitAudioModel>>

    @Query("DELETE FROM chosen_table where id == :id")
    fun deleteChosen(id: Int)

}